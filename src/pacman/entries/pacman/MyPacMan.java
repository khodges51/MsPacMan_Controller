package pacman.entries.pacman;

import static pacman.game.Constants.EDIBLE_TIME;
import static pacman.game.Constants.EDIBLE_TIME_REDUCTION;
import static pacman.game.Constants.LEVEL_RESET_REDUCTION;

import java.awt.Color;
import java.util.ArrayList;
import java.util.PriorityQueue;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;
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
	 * Create a new controller for Ms. Pac-MAn
	 * @param network
	 * 			The neural network that the controller will use to make decisions
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
		int bestMoveIndex = 0;
		double largestOutput = -1.0;
		//FOR EACH DIRECTION
		for(int i = 0; i < 4; i++){
			double[] networkInputs = new double[Executor.netInputs];
			double networkOutput;
			MOVE direction = MOVE.getByIndex(i);
			
			//GET NON-DIRECTIONAL INPUTS
			networkInputs = getConstantInputs(networkInputs, 0, game);
			
			//If the move is possible
			if(game.isMovePossible(direction)){
				//GET DIRECTIONAL INPUTS
				networkInputs = getDirectionalInputs(networkInputs, 6, direction, game);
				//FEED THE NETWORK INPUTS AND STORE OUTPUT
				networkOutput = runNetwork(networkInputs);
			}else/*Else if the move isn't possible*/{
				//Set the output to be the lowest possible value
				networkOutput = -1.0;
			}
			
			 //COMPARE OUTPUT WITH OTHER DIRECTIONS
			 if(networkOutput > largestOutput){
				 bestMoveIndex = i;
				 largestOutput = networkOutput;
			 }
			 
			//consoleOut_inputsAndOutputs(direction, networkInputs, networkOutput);
		}
		//System.out.println();
		
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
		
		//GET POPORTION OF REMAINING PILLS
		double amountPillsLeft = (double)game.getNumberOfActivePills() / (double)game.getNumberOfPills();
		networkInputs[startIndex] = amountPillsLeft;
		
		//GET POPORTION OF REMAINING Power PILLS
		double amountPowerPillsLeft = (double)game.getNumberOfActivePowerPills() / (double)game.getNumberOfPowerPills();
		networkInputs[startIndex + 1] = amountPowerPillsLeft;
		
		/*
		 * SPRINT 3 MOCK CODE
		 */
		double numberOfEdibleGhosts = 0;
		double maximumEdibleTime=EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,game.getCurrentLevel()%LEVEL_RESET_REDUCTION));
		double currentEdibleTime = 0.0;
		
		for(GHOST ghost : GHOST.values()){
			if(game.getGhostEdibleTime(ghost) > 0){
				numberOfEdibleGhosts++;
				currentEdibleTime = game.getGhostEdibleTime(ghost);
			}
		}
		double propOfEdibleGhosts = numberOfEdibleGhosts / 4.0;
		networkInputs[startIndex + 2] = propOfEdibleGhosts;
		double propEdibleTime = currentEdibleTime / maximumEdibleTime;
		networkInputs[startIndex + 3] = propEdibleTime;
		
		//Is any ghost edible?
		if(numberOfEdibleGhosts > 0){
			networkInputs[startIndex + 4] = dataNormalizer.normalizeBoolean(true);
		}else{
			networkInputs[startIndex + 4] = dataNormalizer.normalizeBoolean(false);
		}
		
		//Are we 10 steps away from a power pill?
		boolean isXStepsAway = false;
		int closestPowerPillIndex = game.getClosestNodeIndexFromNodeIndex(pacManIndex, game.getActivePowerPillsIndices(), DM.PATH);
		if(closestPowerPillIndex != -1){
			if(game.getShortestPathDistance(pacManIndex, closestPowerPillIndex) <= 20){
				isXStepsAway = true;
			}
		}
		networkInputs[startIndex + 5] = dataNormalizer.normalizeBoolean(isXStepsAway);
		/*
		 * 
		 */
		
		/*
		 * SPRINT 4 MOCK CODE
		 */
		
		/*
		 * 
		 */
		
		return networkInputs;
	}
	 
	/*
	 *  Returns the neural network inputs for the given direction
	 */
	private double[] getDirectionalInputs(double[] networkInputs, int startIndex, MOVE direction, Game game){
		
		networkInputs = getGhostInfo(networkInputs, startIndex, direction, game);
		
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		
		//Get distance to closest pill
		networkInputs[startIndex + 16] = dataNormalizer.normalizeInput(getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePillsIndices(), 200, game), 200);
		
		//Get distance to closest power pill
		networkInputs[startIndex + 17] = dataNormalizer.normalizeInput(getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePowerPillsIndices(), 200, game), 200);
		
		//Get distance to closest junction
		networkInputs[startIndex + 18] = dataNormalizer.normalizeInput(getDirectionalDistanceToNearest(direction, pacManIndex, game.getJunctionIndices(), 200, game), 200);
		
		/*
		 * SPRINT 4 MOCK CODE
		 */
		//Is the nearest junction blocked?
		double maxDistance = 200;
		int closestNode = game.getClosestNodeIndexFromNodeIndex_Directional(pacManIndex, game.getJunctionIndices(), direction, maxDistance);
		int[] pathToJunction;
		
		if(closestNode != -1){
			pathToJunction = game.getShortestPath_absolute(pacManIndex, closestNode, direction);
			
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
			
			networkInputs[startIndex + 19] = dataNormalizer.normalizeBoolean(isBlocked);
		}else{
			//Set input to false? No closest junction found...
			networkInputs[startIndex + 19] = dataNormalizer.normalizeBoolean(false);
		}
		
		//MAX PILLS IN 30 STEPS
		networkInputs[startIndex + 20] = dataNormalizer.normalizeInput(maxXIn40Steps(direction, pacManIndex, game.getActivePillsIndices(), game), 10);
		
		//MAX JUNCTIONS IN 30 STEPS
		networkInputs[startIndex + 21] = dataNormalizer.normalizeInput(maxXIn40Steps(direction, pacManIndex, game.getJunctionIndices(), game), 10);
		
		/*
		 * 
		 */
		
		return networkInputs;
	}
	
	/*
	 * Adds inputs concerning directional information about each ghost into the network. Ghosts are ordered from closest to farthest.
	 */
	private double[] getGhostInfo(double[] networkInputs, int startIndex, MOVE direction, Game game){
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		
		PriorityQueue<GhostTracker> orderedGhosts = new PriorityQueue<GhostTracker>(4, new GhostTrackerDirectionalComparator(direction));
		for(GHOST ghost : GHOST.values()){
			GhostTracker ghostTracker = new GhostTracker(ghost, game);
			orderedGhosts.add(ghostTracker);
		}
		for(int j = startIndex; j < startIndex + 4; j++){
			GhostTracker ghostTracker = orderedGhosts.poll();
			networkInputs[j] = dataNormalizer.normalizeInput(ghostTracker.getDirectionalDistance(direction), 200);
			networkInputs[j + 4] = dataNormalizer.normalizeBoolean(ghostTracker.isEdible());
			networkInputs[j + 8] = dataNormalizer.normalizeBoolean(ghostTracker.doesPathContainJunction(direction));
			
			/*
			 * SPRINT 4 MOCK CODE
			 */
			//Is the ghost approaching from this direction?
			//This is a simplified interpretation of the story. I could possibly take into account ghosts last direction of travel/non-reversal
			//rule to come up with a more comprehensive implementation. 
			
			//Find the direction you would travel from Ms.Pac-Man to the ghost. This is probably the optimal path and so the direction
			//of approach.
			MOVE incomingDirection = game.getNextMoveTowardsTarget(pacManIndex, game.getGhostCurrentNodeIndex(ghostTracker.getGhost()), DM.PATH);
			if(incomingDirection == direction){
				networkInputs[j + 12] = dataNormalizer.normalizeBoolean(true);
			}else{
				networkInputs[j + 12] = dataNormalizer.normalizeBoolean(false);
			}
			/*
			 * 
			 */
		}
		
		return networkInputs;
	}
	
	private double maxXIn40Steps(MOVE direction, int startIndex, int[] targetNodeIndicies, Game game){
		double matchesSoFar = 0;
		//Needs to track the max pills found over all branches spawned from this branch, including this branch
		double maxMatchesSoFar = 0;
		//Start search in neighbouring node
		int currentIndex = game.getNeighbour(startIndex, direction);
		
		for(int stepsSoFar = 0; stepsSoFar < 40; stepsSoFar++){
			//If current index is a match
			for(int i = 0; i < targetNodeIndicies.length; i++){
				if(targetNodeIndicies[i] == currentIndex){
					matchesSoFar++;
				}
			}
			
			//Find next neighbour
			int[] neighbours = game.getNeighbouringNodes(currentIndex, direction);
			
			//If we are at a junction
			for(int i = 1; i < neighbours.length; i++){
				int nodeIndex = neighbours[i];
				//Start a new branch for this neighbour
				double matchesInNewBranch = maxXIn40Steps(game.getMoveToMakeToReachDirectNeighbour(currentIndex, nodeIndex), nodeIndex, targetNodeIndicies, stepsSoFar + 1, matchesSoFar, game);
				
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
	
	private double maxXIn40Steps(MOVE direction, int startIndex, int[] targetNodeIndicies, int stepsPreviously, double matchesSoFar, Game game){
		//Needs to track the max pills found over all branches spawned from this branch, including this branch
		double maxMatchesSoFar = matchesSoFar;
		int currentIndex = startIndex;
		
		//Find next neighbour
		for(int stepsSoFar = stepsPreviously; stepsSoFar < 40; stepsSoFar++){
			//If current index is a match
			for(int i = 0; i < targetNodeIndicies.length; i++){
				if(targetNodeIndicies[i] == currentIndex){
					matchesSoFar++;
				}
			}
			
			//Find next neighbour
			int[] neighbours = game.getNeighbouringNodes(currentIndex, direction);
			
			//If we are at a junction
			for(int i = 1; i < neighbours.length; i++){
				int nodeIndex = neighbours[i];
				//Start a new branch for this neighbour
				double matchesInNewBranch = maxXIn40Steps(game.getMoveToMakeToReachDirectNeighbour(currentIndex, nodeIndex), nodeIndex, targetNodeIndicies, stepsSoFar + 1, matchesSoFar, game);
				
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
	
	/*
	 * Output to the console the current direction, whether it is a possible move, and the
	 * network output.
	 */
	private void consoleOut_isMovePossible(MOVE direction, Game game, double networkOutput){
		System.out.print("Direction: ");
		System.out.print(direction);
		System.out.print(" || IsPossible?: ");
		System.out.print(game.isMovePossible(direction));
		System.out.print(" || Net output: ");
		System.out.println(networkOutput);
	}
	
	/*
	 * Output to the console the network inputs and outputs and the current direction
	 */
	private void consoleOut_inputsAndOutputs(MOVE direction, double[] networkInputs, double networkOutput){
		System.out.print("Direction: ");
		System.out.println(direction);
		System.out.print("Network inputs: ");
		System.out.println(stringifyArray(networkInputs));
		System.out.print("Network output");
		System.out.println(networkOutput);
	}
	
	/*
	 * Output to the console information about the decision the made by the controller
	 */
	private void consoleOut_chosenMove(MOVE direction){
		System.out.print("Chosen Move: ");
		System.out.println(direction);
	}
	
	/*
	 * Return the given array of numbers in text form
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