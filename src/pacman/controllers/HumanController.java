package pacman.controllers;

import static pacman.game.Constants.EDIBLE_TIME;
import static pacman.game.Constants.EDIBLE_TIME_REDUCTION;
import static pacman.game.Constants.LEVEL_RESET_REDUCTION;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.PriorityQueue;

import pacman.Executor;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.entries.pacman.*;

/**
 * 
 * This class allows for a human to control Ms. Pac-Man.
 * I will adapt it into a test class for inputs, visual tests
 * showing if inputs are working and console outputs.
 * 
 * @author joseatovar
 * 		   https://github.com/joseatovar,
 * 		   Kurt Hodges
 * 		   kuh1@aber.ac.uk
 */
public class HumanController extends Controller<MOVE>
{
	
public KeyBoardInput input;
		
	private DataNormalizer dataNormalizer;
	private Color drawColor;
	private Color directionalColors[] = {Color.red, Color.green, Color.yellow, Color.blue};
    private MOVE testDirection = MOVE.LEFT;
	
    public HumanController(KeyBoardInput input)
    {
    	this.input=input;
    	dataNormalizer = new DataNormalizer();
    }
    
    public KeyBoardInput getKeyboardInput()
    {
    	return input;
    }

    public MOVE getMove(Game game,long dueTime)
    {	
    	
    	MOVE chosenDir = getMoveTest(game, dueTime);
    	drawColor = Color.CYAN;
    	
    	//Return a move based on keyboard input
    	switch(input.getKey())
    	{
	    	case KeyEvent.VK_UP: 	return MOVE.UP;
	    	case KeyEvent.VK_RIGHT: return MOVE.RIGHT;
	    	case KeyEvent.VK_DOWN: 	return MOVE.DOWN;
	    	case KeyEvent.VK_LEFT: 	return MOVE.LEFT;
	    	default: 				return MOVE.NEUTRAL;
    	}
    }
	
    /*
     * 
     * 
     * 
     * 
     */
	
    /**
	 * This is the only method that needs to be implemented by contestants, it simply returns
	 * the direction to move this game cycle. 
	 */
	public MOVE getMoveTest(Game game, long timeDue) 
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
			drawColor = directionalColors[i];
			
			//If the move is possible
			if(game.isMovePossible(direction)){
				
				//Get direction dependent inputs
				networkInputs = getDirectionalInputs(networkInputs, 6, direction, game);
				
			}else/*Else if the move isn't possible*/{
				//Set the output to be the lowest possible value
				networkOutput = -1.0;
			}
			//consoleOut_inputs(direction, networkInputs);
		}
		System.out.println();
		
		return MOVE.getByIndex(bestMoveIndex);
	}
	
	/*
	 *  Returns the neural network inputs that do'nt depend upon direction
	 */
	private double[] getConstantInputs(double[] networkInputs, int startIndex, Game game){
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		
		//Get proportion of remaining pills
		double amountPillsLeft = (double)game.getNumberOfActivePills() / (double)game.getNumberOfPills();
		networkInputs[startIndex] = amountPillsLeft;
		//System.out.print("Proportion of remaining pills: ");
		//System.out.println(networkInputs[startIndex]);
		//System.out.println();
		
		//Get proportion of remaining power pills
		double amountPowerPillsLeft = (double)game.getNumberOfActivePowerPills() / (double)game.getNumberOfPowerPills();
		networkInputs[startIndex + 1] = amountPowerPillsLeft;
		//System.out.print("Proportion of remaining power pills: ");
		//System.out.println(networkInputs[startIndex + 1]);
		//System.out.println();
		
		//Are we 10 steps away from a power pill?
		networkInputs[startIndex + 2] = dataNormalizer.normalizeBoolean(isXStepsAway(pacManIndex, game.getActivePowerPillsIndices(), 10, game));
		//System.out.print("Are we 10 steps away from a power pill: ");
		//System.out.println(networkInputs[startIndex + 2]);
		//System.out.println();
		
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
		//System.out.print("Proportion of edible ghosts: ");
		//System.out.println(networkInputs[startIndex]);
		//System.out.println();
		
		//Get the proportion of time remaining of ghosts being edible
		double propEdibleTime = currentEdibleTime / maximumEdibleTime;
		networkInputs[startIndex + 1] = propEdibleTime;
		//System.out.print("Proportion of edible time: ");
		//System.out.println(networkInputs[startIndex + 1]);
		//System.out.println();
		
		//Is any ghost edible?
		if(numberOfEdibleGhosts > 0){
			networkInputs[startIndex + 2] = dataNormalizer.normalizeBoolean(true);
		}else{
			networkInputs[startIndex + 2] = dataNormalizer.normalizeBoolean(false);
		}
		//System.out.print("Is any ghost edible?: ");
		//System.out.println(networkInputs[startIndex + 2]);
		//System.out.println();
		
		return networkInputs;
	}
	 
	/*
	 *  Returns the neural network inputs for the given direction
	 */
	private double[] getDirectionalInputs(double[] networkInputs, int startIndex, MOVE direction, Game game){
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		
		//Get distance to closest pill
		double input = getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePillsIndices(), 200, game, false);
		networkInputs[startIndex] = dataNormalizer.normalizeDouble(input, 200);
		//System.out.println(direction);
		//System.out.print("Direction to nearest pill? [NOT SCALED]: ");
		//System.out.println(input);
		//System.out.print("Distance to nearest pill? [SCALED]: ");
		//System.out.println(networkInputs[startIndex]);
		
		//Get distance to closest power pill
		input = getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePowerPillsIndices(), 200, game, false);
		networkInputs[startIndex + 1] = dataNormalizer.normalizeDouble(input, 200);
		//System.out.println(direction);
		//System.out.print("Direction to nearest power pill? [NOT SCALED]: ");
		//System.out.println(input);
		//System.out.print("Distance to nearest power Pill? [SCALED]: ");
		//System.out.println(networkInputs[startIndex + 1]);
		
		//Get distance to closest junction
		input = getDirectionalDistanceToNearest(direction, pacManIndex, game.getJunctionIndices(), 200, game, false);
		networkInputs[startIndex + 2] = dataNormalizer.normalizeDouble(input, 200);
		//System.out.println(direction);
		//System.out.print("Direction to nearest junction? [NOT SCALED]: ");
		//System.out.println(input);
		//System.out.print("Distance to nearest junction? [SCALED]: ");
		//System.out.println(networkInputs[startIndex + 2]);
		
		//Is the nearest junction blocked?
		networkInputs[startIndex + 3] = dataNormalizer.normalizeBoolean(isNearestJunctionBlocked(pacManIndex, direction, game, false));
		//System.out.println(direction);
		//System.out.print("Is the nearest junction blocked?: ");
		//System.out.println(networkInputs[startIndex + 3]);
		
		//Max pills in 40 steps
		input = maxIn40Steps(
				direction, game.getNeighbour(pacManIndex, direction), game.getActivePillsIndices(), game);
		networkInputs[startIndex + 4] = dataNormalizer.normalizeDouble(input, 10);
		//System.out.println(direction);
		//System.out.print("Max pills in 40 steps [NOT SCALED]: ");
		//System.out.println(input);
		//System.out.print("Max pills in 40 steps [SCALED]: ");
		//System.out.println(networkInputs[startIndex + 4]);
		
		//Max junctions in 40 steps
		input = maxIn40Steps(
				direction, game.getNeighbour(pacManIndex, direction), game.getJunctionIndices(), game);
		networkInputs[startIndex + 5] = dataNormalizer.normalizeDouble(input, 10);
		//System.out.println(direction);
		//System.out.print("Max junctions in 40 steps [NOT SCALED]: ");
		//System.out.println(input);
		//System.out.print("Max junctions in 40 steps [SCALED]: ");
		//System.out.println(networkInputs[startIndex + 5]);
		
		//Get the ghost related inputs
		//For testing, only wanna check one direction at a time
		if(direction == testDirection){
			System.out.println(direction);
			networkInputs = getDirectionalGhostInfo(networkInputs, startIndex + 6, direction, game);
		}
		
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
			System.out.print("Ghost: ");
			System.out.println(ghostTracker.getGhost());
			
			//Sort the color for debugging
			if(ghostTracker.getGhost() == GHOST.BLINKY){
				drawColor = Color.RED;
			}else if(ghostTracker.getGhost() == GHOST.INKY){
				drawColor = Color.CYAN;
			}else if(ghostTracker.getGhost() == GHOST.SUE){
				drawColor = Color.ORANGE;
			}else if(ghostTracker.getGhost() == GHOST.PINKY){
				drawColor = Color.PINK;
			}
			
			//Distance to ghost
			double input = ghostTracker.getDirectionalDistanceDrawn(direction, drawColor);
			networkInputs[j] = dataNormalizer.normalizeDouble(input, 200);
			System.out.print("Distance to ghost [NOT SCALED]: ");
			System.out.println(input);
			System.out.print("Distance to ghost [SCALED]: ");
			System.out.println(networkInputs[j]);
			
			//Is it edible?
			networkInputs[j + 4] = dataNormalizer.normalizeBoolean(ghostTracker.isEdible());
			//System.out.print("Is edible?: ");
			//System.out.println(networkInputs[j + 4]);
			
			//Does the path contain a junction?
			networkInputs[j + 8] = dataNormalizer.normalizeBoolean(ghostTracker.doesPathContainJunction(direction));
			//System.out.print("Does path contain junction?: ");
			//System.out.println(networkInputs[j + 8]);
			
			//Is the ghost approaching?
			networkInputs[j + 12] = dataNormalizer.normalizeBoolean(ghostTracker.isGhostApproaching(direction, pacManIndex));
			//System.out.print("Is approaching?: ");
			//System.out.println(networkInputs[j + 12]);
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
			//Draw for testing
			//GameView.addPoints(game,drawColor,game.getShortestPath(game.getPacmanCurrentNodeIndex(),closestIndex));
		}
		
		return isXStepsAway;
	}
	
	/*
	 * Is the nearest junction blocked?
	 */
	private boolean isNearestJunctionBlocked(int startIndex, MOVE direction, Game game, boolean isDrawn){
		double maxDistance = 200;
		int closestNode = game.getClosestNodeIndexFromNodeIndex_directional(startIndex, game.getJunctionIndices(), direction, maxDistance);
		int[] pathToJunction;
		
		if(closestNode != -1){
			pathToJunction = game.getShortestPath_absolute(startIndex, closestNode, direction);
			
			if(isDrawn){
				GameView.addPoints(game,drawColor,pathToJunction);
			}
			
			int pinkyIndex = -50000;
			int blinkyIndex = -50000;
			int inkyIndex = -50000;
			int sueIndex = -50000;
			
			if(!game.isGhostEdible(GHOST.PINKY))
				pinkyIndex = game.getGhostCurrentNodeIndex(GHOST.PINKY);
			if(!game.isGhostEdible(GHOST.BLINKY))
				blinkyIndex = game.getGhostCurrentNodeIndex(GHOST.BLINKY);
			if(!game.isGhostEdible(GHOST.INKY))
				inkyIndex = game.getGhostCurrentNodeIndex(GHOST.INKY);
			if(!game.isGhostEdible(GHOST.SUE))
				sueIndex = game.getGhostCurrentNodeIndex(GHOST.SUE);
			
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
			
			//Draw for testing
			//GameView.addPoints(game,drawColor,neighbours[0]);
			
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
	private double getDirectionalDistanceToNearest(MOVE direction, int searchStartIndex, int[] targetIndicies, double maxDistance, Game game, boolean doDraw){
		double distance = maxDistance;
		int closestNode = game.getClosestNodeIndexFromNodeIndex_directional(searchStartIndex, targetIndicies, direction, maxDistance);
		
		if(closestNode != -1){
			distance = game.getShortestPathDistance_absolute(searchStartIndex, closestNode, direction);
			if(doDraw){
				//Draw for testing
				GameView.addPoints(game,drawColor,game.getShortestPath_absolute(game.getPacmanCurrentNodeIndex(),closestNode, direction));
			}
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
	private void consoleOut_inputs(MOVE direction, double[] networkInputs){
		System.out.print("Direction: ");
		System.out.println(direction);
		System.out.print("Network inputs: ");
		System.out.println(stringifyArray(networkInputs));
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