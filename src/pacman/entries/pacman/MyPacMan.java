package pacman.entries.pacman;

import static pacman.game.Constants.EDIBLE_TIME;
import static pacman.game.Constants.EDIBLE_TIME_REDUCTION;
import static pacman.game.Constants.LEVEL_RESET_REDUCTION;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import jneat.*;

/**
 * 
 * This class is where contestants put their code for Ms. Pac-Man, deciding which direction to
 * travel each game cycle. My implementation will input information into a neural network for
 * each possible direction of travel, and evaluate the output to determine which direction is most
 * suitable. 
 * 
 * @author Kurt Hodges
 * 		   kuh1@aber.ac.uk
 *
 */
public class MyPacMan extends Controller<MOVE>
{
	private Network network;
	private DataNormalizer dataNormalizer;
	
	/**
	 * Create a new controller for Ms. Pac-Man
	 * @param network The neural network that the controller will use to make decisions
	 */
	public MyPacMan(Network network){
		this.network = network;
		dataNormalizer = new DataNormalizer();
	}
	
	/**
	 * This is the only method that needs to be implemented by contestants, it simply returns
	 * the direction to move this game cycle. 
	 */
	public MOVE getMove(Game game, long timeDue) 
	{			
		double[] networkInputs = new double[Executor.netInputs];
		
		int bestMoveIndex = 0;
		double largestOutput = -1.0;
		
		//Get non directional inputs
		networkInputs = getConstantInputs(networkInputs, 0, game);
		
		//For each direction
		for(int i = 0; i < 4; i++){
			double networkOutput;
			MOVE direction = MOVE.getByIndex(i);
			
			//If the move is possible
			if(game.isMovePossible(direction)){
				
				//Get direction dependent inputs
				networkInputs = getDirectionalInputs(networkInputs, 6, direction, game);
				//Feed the network all of the inputs and capture the output
				networkOutput = runNetwork(networkInputs);
				
			}else/*Else if the move isn't possible*/{
				//Set the output to be the lowest possible value
				networkOutput = -1.0;
			}
			
			 //Compare the output with output for each other direction
			 if(networkOutput > largestOutput){
				 bestMoveIndex = i;
				 largestOutput = networkOutput;
			 }
			 
			consoleOut_inputsAndOutputs(direction, networkInputs, networkOutput);
		}
		System.out.println();
		
		return MOVE.getByIndex(bestMoveIndex);
	}
	
	/*
	 *  Loads the neural network with the given inputs and returns the output
	 */
	private double runNetwork(double[] networkInputs){
		network.load_sensors(networkInputs);
		 
		 int net_depth = network.max_depth();
		 // first activate from sensor to next layer....
		 network.activate();
		 // next activate each layer until the last level is reached
		 for (int relax = 0; relax <= net_depth; relax++)
		 {
			 network.activate();
		 }
		 
		 double output = ((NNode) network.getOutputs().elementAt(0)).getActivation();
		 network.flush();
		 return output;
	}
	
	/*
	 *  Returns the neural network inputs that do'nt depend upon direction
	 */
	private double[] getConstantInputs(double[] networkInputs, int startIndex, Game game){
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		
		//Get proportion of remaining pills
		double amountPillsLeft = (double)game.getNumberOfActivePills() / (double)game.getNumberOfPills();
		networkInputs[startIndex] = amountPillsLeft;
		
		//Get proportion of remaining power pills
		double amountPowerPillsLeft = (double)game.getNumberOfActivePowerPills() / (double)game.getNumberOfPowerPills();
		networkInputs[startIndex + 1] = amountPowerPillsLeft;

		//Are we 20 steps away from a power pill?
		networkInputs[startIndex + 2] = dataNormalizer.normalizeBoolean(isXStepsAway(pacManIndex, game.getActivePowerPillsIndices(), 20, game));
		
		//Get the ghost related inputs
		networkInputs = getConstantGhostInfo(networkInputs, startIndex + 3, game);

		return networkInputs;
	}
	
	/*
	 * Adds inputs concerning information about all of the ghost into the network
	 */
	private double[] getConstantGhostInfo(double[] networkInputs, int startIndex, Game game){
		double maximumEdibleTime=EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,game.getCurrentLevel()%LEVEL_RESET_REDUCTION));
		double currentEdibleTime = 0.0;
		double numberOfEdibleGhosts = 0;
		
		for(GHOST ghost : GHOST.values()){
			if(game.getGhostEdibleTime(ghost) > 0){
				numberOfEdibleGhosts++;
				currentEdibleTime = game.getGhostEdibleTime(ghost);
			}
		}
		
		//Get the proportion of ghosts that are edible
		double propOfEdibleGhosts = numberOfEdibleGhosts / 4.0;
		networkInputs[startIndex] = propOfEdibleGhosts;
		
		//Get the proportion of time remaining of ghosts being edible
		double propEdibleTime = currentEdibleTime / maximumEdibleTime;
		networkInputs[startIndex + 1] = propEdibleTime;
		
		//Is any ghost edible?
		if(numberOfEdibleGhosts > 0){
			networkInputs[startIndex + 2] = dataNormalizer.normalizeBoolean(true);
		}else{
			networkInputs[startIndex + 2] = dataNormalizer.normalizeBoolean(false);
		}
		
		return networkInputs;
	}
	 
	/*
	 *  Returns the neural network inputs for the given direction
	 */
	private double[] getDirectionalInputs(double[] networkInputs, int startIndex, MOVE direction, Game game){
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		
		//Get distance to closest pill
		networkInputs[startIndex] = dataNormalizer.normalizeDouble(getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePillsIndices(), 200, game), 200);
		
		//Get distance to closest power pill
		networkInputs[startIndex + 1] = dataNormalizer.normalizeDouble(getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePowerPillsIndices(), 200, game), 200);
		
		//Get distance to closest junction
		networkInputs[startIndex + 2] = dataNormalizer.normalizeDouble(getDirectionalDistanceToNearest(direction, pacManIndex, game.getJunctionIndices(), 200, game), 200);
		
		//Is the nearest junction blocked?
		networkInputs[startIndex + 3] = dataNormalizer.normalizeBoolean(isNearestJunctionBlocked(pacManIndex, direction, game));
		
		//Max pills in 40 steps
		networkInputs[startIndex + 4] = dataNormalizer.normalizeDouble(maxIn40Steps(
				direction, game.getNeighbour(pacManIndex, direction), game.getActivePillsIndices(), game), 10);
		
		//Max junctions in 40 steps
		networkInputs[startIndex + 5] = dataNormalizer.normalizeDouble(maxIn40Steps(
				direction, game.getNeighbour(pacManIndex, direction), game.getJunctionIndices(), game), 10);
		
		//Get the ghost related inputs
		networkInputs = getDirectionalGhostInfo(networkInputs, startIndex + 6, direction, game);
		
		return networkInputs;
	}
	
	/*
	 * Adds inputs concerning directional information about each ghost into the network. Ghosts are ordered from closest to farthest.
	 */
	private double[] getDirectionalGhostInfo(double[] networkInputs, int startIndex, MOVE direction, Game game){
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		
		PriorityQueue<GhostTracker> orderedGhosts = new PriorityQueue<GhostTracker>(4, new GhostTrackerDirectionalComparator(direction));
		for(GHOST ghost : GHOST.values()){
			GhostTracker ghostTracker = new GhostTracker(ghost, game);
			orderedGhosts.add(ghostTracker);
		}
		for(int j = startIndex; j < startIndex + 4; j++){
			GhostTracker ghostTracker = orderedGhosts.poll();
			networkInputs[j] = dataNormalizer.normalizeDouble(ghostTracker.getDirectionalDistance(direction), 200);
			networkInputs[j + 4] = dataNormalizer.normalizeBoolean(ghostTracker.isEdible());
			networkInputs[j + 8] = dataNormalizer.normalizeBoolean(ghostTracker.doesPathContainJunction(direction));
			networkInputs[j + 12] = dataNormalizer.normalizeBoolean(ghostTracker.isGhostApproaching(direction, pacManIndex));
		}
		
		return networkInputs;
	}
	
	/*
	 * Are you within X steps of one of the target nodes? Searches from the position 'currentIndex'
	 */
	private boolean isXStepsAway(int currentIndex, int[] targetIndicies, int numSteps, Game game){
		boolean isXStepsAway = false;
		
		int closestIndex = game.getClosestNodeIndexFromNodeIndex(currentIndex, targetIndicies, DM.PATH);
		
		if( closestIndex != -1){
			if(game.getShortestPathDistance(currentIndex, closestIndex) <= numSteps){
				isXStepsAway = true;
			}
		}
		
		return isXStepsAway;
	}
	
	/*
	 * Is the nearest junction blocked?
	 */
	private boolean isNearestJunctionBlocked(int startIndex, MOVE direction, Game game){
		double maxDistance = 200;
		int closestNode = game.getClosestNodeIndexFromNodeIndex_Directional(startIndex, game.getJunctionIndices(), direction, maxDistance);
		int[] pathToJunction;
		
		if(closestNode != -1){
			pathToJunction = game.getShortestPath_absolute(startIndex, closestNode, direction);
			
			int pinkyIndex = game.getGhostCurrentNodeIndex(GHOST.PINKY);
			int blinkyIndex = game.getGhostCurrentNodeIndex(GHOST.BLINKY);
			int inkyIndex = game.getGhostCurrentNodeIndex(GHOST.INKY);
			int sueIndex = game.getGhostCurrentNodeIndex(GHOST.SUE);
			
			//Set input to default.....
			boolean isBlocked = false;
			
			for(int i = 0; i < pathToJunction.length; i++){
				if(pathToJunction[i] == pinkyIndex || pathToJunction[i] == sueIndex || pathToJunction[i] == blinkyIndex || pathToJunction[i] == inkyIndex){
					//Path is blocked
					isBlocked = true;
				}
			}
			
			return isBlocked;
		}else{
			//No closest junction found...
			return false;
		}
	}
	
	/*
	 * Searches in the given direction for 40 steps, checking each possible path, to find matches in the 'targetNodeIndicies' array and return
	 * the maximum amount that can be found in 40 steps. E.g maximum number of pills in 40 steps
	 */
	private double maxIn40Steps(MOVE direction, int startIndex, int[] targetNodeIndicies, Game game){
		//Create a hashset to reduce search time to O(1);
		HashSet<Integer> targetIndicies = new HashSet<Integer>();
		
		for(int i = 0; i < targetNodeIndicies.length; i++){
			targetIndicies.add((Integer)targetNodeIndicies[i]);
		}
		
		return maxIn40Steps(direction, startIndex, targetIndicies, 0, 0, game);
	}
	
	/*
	 * Searches in the given direction for 40 steps, checking each possible path, to find matches in the 'targetNodeIndicies' array and return
	 * the maximum amount that can be found in 40 steps. E.g maximum number of pills in 40 steps
	 */
	private double maxIn40Steps(MOVE direction, int startIndex, HashSet<Integer> targetIndicies, int stepsPreviously, double matchesSoFar, Game game){
		int currentIndex = startIndex;
		//Need to track the max/best pills found in this branch and over all branches spawned from this branch
		double maxMatchesSoFar = matchesSoFar;
		
		//Find next neighbour
		for(int stepsSoFar = stepsPreviously; stepsSoFar < 40; stepsSoFar++){
			//If current index is a match
			if(targetIndicies.contains((Integer)currentIndex)){
				matchesSoFar++;
			}
			
			//Find next neighbour
			int[] neighbours = game.getNeighbouringNodes(currentIndex, direction);
			
			//If we are at a junction
			for(int i = 1; i < neighbours.length; i++){
				int nodeIndex = neighbours[i];
				//Start a new branch for this neighbour
				double matchesInNewBranch = maxIn40Steps(game.getMoveToMakeToReachDirectNeighbour(currentIndex, nodeIndex), nodeIndex, targetIndicies, stepsSoFar + 1, matchesSoFar, game);
				
				if(matchesInNewBranch > maxMatchesSoFar)
					maxMatchesSoFar = matchesInNewBranch;
			}
			
			direction = game.getMoveToMakeToReachDirectNeighbour(currentIndex, neighbours[0]);
			currentIndex = neighbours[0];
			stepsSoFar++;
		}
		
		if(matchesSoFar > maxMatchesSoFar)
			maxMatchesSoFar = matchesSoFar;
		
		return maxMatchesSoFar;
	}
	
	/*
	 *  Get the distance to the nearest node from the given position in the given direction. The search will be in the 
	 * 'targetIndicies' array, e.g pass in the indices of all active pills to find the distance to the nearest active pill 
	 *	in the given direction. 
	 */
	private double getDirectionalDistanceToNearest(MOVE direction, int searchStartIndex, int[] targetIndicies, double maxDistance, Game game){
		double distance = maxDistance;
		int closestNode = game.getClosestNodeIndexFromNodeIndex_Directional(searchStartIndex, targetIndicies, direction, maxDistance);
		
		if(closestNode != -1){
			distance = game.getShortestPathDistance_absolute(searchStartIndex, closestNode, direction);
		}
		
		if(distance < maxDistance && distance >= 0){
			return distance;
		}else{
			return maxDistance;
		}
	}
	
	/*
	 * 
	 * 	Below is code for console output. 
	 *  This will most likely only be used for testing, and so should probably be deleted at the
	 *  end of the project. For now proof of testing will be screen shots and tables until I can
	 *  figure out appropriate unit testing.
	 * 
	 */
	
	@SuppressWarnings("unused")
	private void consoleOut_inputsAndOutputs(MOVE direction, double[] networkInputs, double networkOutput){
		System.out.print("Direction: ");
		System.out.println(direction);
		System.out.print("Network inputs: ");
		System.out.println(stringifyArray(networkInputs));
		System.out.print("Network output");
		System.out.println(networkOutput);
	}
	
	/*
	private void consoleOut_isMovePossible(MOVE direction, Game game, double networkOutput){
		System.out.print("Direction: ");
		System.out.print(direction);
		System.out.print(" || IsPossible?: ");
		System.out.print(game.isMovePossible(direction));
		System.out.print(" || Net output: ");
		System.out.println(networkOutput);
	}
	
	private void consoleOut_chosenMove(MOVE direction){
		System.out.print("Chosen Move: ");
		System.out.println(direction);
	}
	*/
	
	private String stringifyArray(double[] networkInputs){
		String text = new String();
		
		text += "(";
		
		for(int i = 0; i < networkInputs.length; i++){
			text += networkInputs[i];
			if(i < networkInputs.length - 1)
				text += ", ";
		}
		
		text += ")";
		
		return text;
	}
}