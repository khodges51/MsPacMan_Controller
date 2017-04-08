package pacman.controllers;

import static pacman.game.Constants.EDIBLE_TIME;
import static pacman.game.Constants.EDIBLE_TIME_REDUCTION;
import static pacman.game.Constants.LEVEL_RESET_REDUCTION;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.PriorityQueue;

import pacman.Executor;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.entries.pacman.*;
import jneat.*;

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
    	
    	getMoveTest(game);
    	
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
	public MOVE getMoveTest(Game game) 
	{			
		double[] networkInputs = new double[Executor.netInputs];
		
		int bestMoveIndex = 0;
		double largestOutput = -1.0;
		
		//Get non directional inputs
		networkInputs = getConstantInputs(networkInputs, 0, game);
		
		//For each direction
		for(int i = 0; i < 4; i++){
			double networkOutput = 0.0;
			MOVE direction = MOVE.getByIndex(i);
			
			//If the move is possible
			if(game.isMovePossible(direction)){
				
				//Get direction dependent inputs
				networkInputs = getDirectionalInputs(networkInputs, 6, direction, game);
				//Feed the network all of the inputs and capture the output
				//networkOutput = runNetwork(networkInputs);
				
			}else/*Else if the move isn't possible*/{
				//Set the output to be the lowest possible value
				networkOutput = -1.0;
			}
			 
			consoleOut_inputsAndOutputs(direction, networkInputs, networkOutput);
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
				
		//Get proportion of remaining power pills
		double amountPowerPillsLeft = (double)game.getNumberOfActivePowerPills() / (double)game.getNumberOfPowerPills();
		networkInputs[startIndex + 1] = amountPowerPillsLeft;
				
		//Get the ghost related inputs
		networkInputs = getConstantGhostInfo(networkInputs, startIndex + 2, game);

		//Are we 20 steps away from a power pill?
		networkInputs[startIndex + 5] = dataNormalizer.normalizeBoolean(isXStepsAway(pacManIndex, game.getActivePowerPillsIndices(), 20, game));

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
		
		//Get the ghost related inputs
		networkInputs = getDirectionalGhostInfo(networkInputs, startIndex, direction, game);
		
		//Get distance to closest pill
		networkInputs[startIndex + 16] = dataNormalizer.normalizeDouble(getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePillsIndices(), 200, game), 200);
		
		//Get distance to closest power pill
		networkInputs[startIndex + 17] = dataNormalizer.normalizeDouble(getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePowerPillsIndices(), 200, game), 200);
		
		//Get distance to closest junction
		networkInputs[startIndex + 18] = dataNormalizer.normalizeDouble(getDirectionalDistanceToNearest(direction, pacManIndex, game.getJunctionIndices(), 200, game), 200);
		
		/*
		 * Is the nearest junction blocked?
		 * Code is working but hideous and probably inefficient
		 */
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
		/*
		 * 
		 */
		
		//Max pills in 40 steps
		networkInputs[startIndex + 20] = dataNormalizer.normalizeDouble(maxIn40Steps(
				direction, game.getNeighbour(pacManIndex, direction), game.getActivePillsIndices(), game), 10);
		
		//Max junctions in 40 steps
		networkInputs[startIndex + 21] = dataNormalizer.normalizeDouble(maxIn40Steps(
				direction, game.getNeighbour(pacManIndex, direction), game.getJunctionIndices(), game), 10);
		
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
	 * Searches in the given direction for 40 steps, checking each possible path, to find matches in the 'targetNodeIndicies' array and return
	 * the maximum amount that can be found in 40 steps. E.g maximum number of pills in 40 steps
	 */
	private double maxIn40Steps(MOVE direction, int startIndex, int[] targetNodeIndicies, Game game){
		return maxIn40Steps(direction, startIndex, targetNodeIndicies, 0, 0, game);
	}
	
	/*
	 * Searches in the given direction for 40 steps, checking each possible path, to find matches in the 'targetNodeIndicies' array and return
	 * the maximum amount that can be found in 40 steps. E.g maximum number of pills in 40 steps
	 */
	private double maxIn40Steps(MOVE direction, int startIndex, int[] targetNodeIndicies, int stepsPreviously, double matchesSoFar, Game game){
		int currentIndex = startIndex;
		//Need to track the max/best pills found in this branch and over all branches spawned from this branch
		double maxMatchesSoFar = matchesSoFar;
		
		//Find next neighbour
		for(int stepsSoFar = stepsPreviously; stepsSoFar < 40; stepsSoFar++){
			//If current index is a match
			//MORE EFFICIENT WAY OF DOING THIS?
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
				double matchesInNewBranch = maxIn40Steps(game.getMoveToMakeToReachDirectNeighbour(currentIndex, nodeIndex), nodeIndex, targetNodeIndicies, stepsSoFar + 1, matchesSoFar, game);
				
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
    
	/*
	public KeyBoardInput input;
	
	public MOVE test_Direction;
    
    public HumanController(KeyBoardInput input)
    {
    	this.input=input;
    }
    
    public KeyBoardInput getKeyboardInput()
    {
    	return input;
    }

    public MOVE getMove(Game game,long dueTime)
    {	
    	MOVE testDirection = MOVE.LEFT;
    	
    	int pacManIndex=game.getPacmanCurrentNodeIndex();
    	
    	//Direction independent inputs
    	
    	//double amountPowerPillsLeft = (double)game.getNumberOfActivePowerPills() / (double)game.getNumberOfPowerPills();
		//System.out.println(amountPowerPillsLeft);
    	
    	//double amountPillsLeft = (double)game.getNumberOfActivePills() / (double)game.getNumberOfPills();
    	//System.out.println(amountPillsLeft);
    	
    	//test_propEdibleGhosts(game);
    	//test_propEdibleTime(game);
    	//test_areTenStepsFromPowerPill(game);
    	//test_areAnyGhostsEdible(game);
    	//test_isTenStepsAway(game);
    	
    	//Direction specific tests
    	if(game.isMovePossible(testDirection)){
    		//Run some tests to help verify if stories are complete
        	//test_DirectionalDistanceToGhosts(game, testDirection);
    		//test_DirectionalDistanceToNearestPill(game, testDirection, Color.red);
    		//test_DirectionalDistanceToNearestPowerPill(game, testDirection, Color.red);
    		//test_DirectionalDistanceToNearestJunction(game, testDirection, Color.red);
    		//test_DoesPathContainJunction(game, testDirection);
    		//test_isGhostApproachingFromGivenDirection(game, testDirection, GHOST.PINKY);
    		
    		System.out.print("DIRECTION: ");
    		System.out.println(testDirection);
    		
    		//Is the nearest junction blocked?
    		System.out.print("Is the junction in this direction blocked?: ");
    		double maxDistance = 200;
    		int closestNode = game.getClosestNodeIndexFromNodeIndex_Directional(pacManIndex, game.getJunctionIndices(), testDirection, maxDistance);
    		int[] pathToJunction;
    		double output;
    		
    		if(closestNode != -1){
    			pathToJunction = game.getShortestPath_absolute(pacManIndex, closestNode, testDirection);
    			GameView.addPoints(game,Color.red,pathToJunction);
    			
    			int pinkyIndex = game.getGhostCurrentNodeIndex(GHOST.PINKY);
    			int blinkyIndex = game.getGhostCurrentNodeIndex(GHOST.BLINKY);
    			int inkyIndex = game.getGhostCurrentNodeIndex(GHOST.INKY);
    			int sueIndex = game.getGhostCurrentNodeIndex(GHOST.SUE);
    			
    			//Set input to 0.0? Need to default.....
    			output = 0.0;
    			
    			for(int i = 0; i < pathToJunction.length; i++){
    				if(pathToJunction[i] == pinkyIndex || pathToJunction[i] == sueIndex || pathToJunction[i] == blinkyIndex || pathToJunction[i] == inkyIndex){
    					//Path is blocked
    					output = 1.0;
    				}
    			}
    			
    			
    		}else{
    			//Set input to 0.0? No closest junction found...
    			output = 0.0;
    		}
    		System.out.println(output);
    		
    		
    		test_Direction = testDirection;
    		//MAX PILLS IN 40 Steps
    		System.out.println("SEARCHING FOR PILLS");
    		System.out.println(test_maxXIn40Steps(testDirection, game.getNeighbour(pacManIndex, testDirection), game.getActivePillsIndices(), 0, 0, game));
    
    		
    	}
    	
    	//Tests for all 4 directions
    	Color[] colors = {Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN};
    	for(int i = 0; i < 4; i++){
    		testDirection = MOVE.getByIndex(i);
    		if(game.isMovePossible(testDirection)){
            	//test_DirectionalDistanceToNearestPill(game, testDirection, colors[i]);
        		//test_DirectionalDistanceToNearestPowerPill(game, testDirection, colors[i]);
        		//test_DirectionalDistanceToNearestJunction(game, testDirection, colors[i]);
        	}
    	}
    	
    	System.out.println();
    	
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
    
    private double test_maxXIn40Steps(MOVE direction, int startIndex, int[] targetNodeIndicies, int stepsPreviously, double matchesSoFar, Game game){
		int currentIndex = startIndex;
		//Need to track the max/best pills found in this branch and over all branches spawned from this branch
		double maxMatchesSoFar = matchesSoFar;
		
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
				double matchesInNewBranch = test_maxXIn40Steps(game.getMoveToMakeToReachDirectNeighbour(currentIndex, nodeIndex), nodeIndex, targetNodeIndicies, stepsSoFar + 1, matchesSoFar, game);
				
				if(matchesInNewBranch > maxMatchesSoFar)
					maxMatchesSoFar = matchesInNewBranch;
			}
			
			direction = game.getMoveToMakeToReachDirectNeighbour(currentIndex, neighbours[0]);
			currentIndex = neighbours[0];
			stepsSoFar++;
		}
		
		if(matchesSoFar > maxMatchesSoFar)
			maxMatchesSoFar = matchesSoFar;
		
		GameView.addPoints(game,Color.red,game.getShortestPath_absolute(game.getPacmanCurrentNodeIndex(), currentIndex, test_Direction));
		
		return maxMatchesSoFar;
	}
    
    private void test_propEdibleGhosts(Game game){
    	System.out.print("Prop of edible ghosts: ");
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
		System.out.println(propOfEdibleGhosts);
    }
    
    private void test_propEdibleTime(Game game){
    	System.out.print("Prop of edible time: ");
    	double numberOfEdibleGhosts = 0;
		double maximumEdibleTime=EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,game.getCurrentLevel()%LEVEL_RESET_REDUCTION));
		double currentEdibleTime = 0.0;
		
		for(GHOST ghost : GHOST.values()){
			if(game.getGhostEdibleTime(ghost) > 0){
				numberOfEdibleGhosts++;
				currentEdibleTime = game.getGhostEdibleTime(ghost);
			}
		}
		double propEdibleTime = currentEdibleTime / maximumEdibleTime;
		System.out.println(propEdibleTime);
    }
    

    private void test_isTenStepsAway(Game game){
    	int pacManIndex=game.getPacmanCurrentNodeIndex();
    	System.out.print("Are we 20 steps away from a power pill?: ");
    	//Are we 10 steps away from a power pill?
    	double isTenStepsAway = 0.0;
    	int closestPowerPillIndex = game.getClosestNodeIndexFromNodeIndex(pacManIndex, game.getActivePowerPillsIndices(), DM.PATH);
    	if(closestPowerPillIndex != -1){
    		if(game.getShortestPathDistance(pacManIndex, closestPowerPillIndex) <= 20){
    			isTenStepsAway = 1.0;
    			GameView.addPoints(game,Color.red,game.getShortestPath(pacManIndex, closestPowerPillIndex));
    		}
    	}
    	System.out.println(isTenStepsAway);
    }
    
    private void test_areAnyGhostsEdible(Game game){
    	System.out.print("Are any ghosts edible?: ");
    	double numberOfEdibleGhosts = 0;
		double maximumEdibleTime=EDIBLE_TIME*(Math.pow(EDIBLE_TIME_REDUCTION,game.getCurrentLevel()%LEVEL_RESET_REDUCTION));
		double currentEdibleTime = 0.0;
		
		for(GHOST ghost : GHOST.values()){
			if(game.getGhostEdibleTime(ghost) > 0){
				numberOfEdibleGhosts++;
				currentEdibleTime = game.getGhostEdibleTime(ghost);
			}
		}
		
		//Is any ghost edible?
		if(numberOfEdibleGhosts > 0){
			System.out.println(1.0);
		}else{
			System.out.println(0.0);
		}
    }
    
    private void test_DirectionalDistanceToGhosts(Game game, MOVE direction){
    	
		System.out.print("DIRECTION: ");
		System.out.println(direction);
	
    
		Color[] colors = {Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN};
		
    	//If the move is possible
		if(game.isMovePossible(direction)){	
			//GHOST DISTANCE 1st to 4th and are they edible??
			PriorityQueue<GhostTracker> orderedGhosts = new PriorityQueue<GhostTracker>(4, new GhostTrackerDirectionalComparator(direction));
			for(GHOST ghost : GHOST.values()){
				GhostTracker ghostTracker = new GhostTracker(ghost, game);
				orderedGhosts.add(ghostTracker);
			}
			for(int j = 0; j < 4; j++){
				GhostTracker ghostTracker = orderedGhosts.poll();
				
				System.out.print("GHOST: ");
				System.out.print(ghostTracker.getGhost());	
				System.out.print(" || DISTANCE: ");
				System.out.println(ghostTracker.getDirectionalDistance_drawn(direction, colors[j]));	
				
			}
			
		}else{
			System.out.println("NOT POSSIBLE MOVE");
		}
		System.out.println();
    }
    

    private void test_DoesPathContainJunction(Game game, MOVE direction){
    	
		System.out.print("DIRECTION: ");
		System.out.println(direction);
	
    
		Color[] colors = {Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN};
		
    	//If the move is possible
		if(game.isMovePossible(direction)){	
			//GHOST DISTANCE 1st to 4th and are they edible??
			PriorityQueue<GhostTracker> orderedGhosts = new PriorityQueue<GhostTracker>(4, new GhostTrackerDirectionalComparator(direction));
			for(GHOST ghost : GHOST.values()){
				GhostTracker ghostTracker = new GhostTracker(ghost, game);
				orderedGhosts.add(ghostTracker);
			}
			for(int j = 0; j < 4; j++){
				GhostTracker ghostTracker = orderedGhosts.poll();
				System.out.print("GHOST: ");
				System.out.print(ghostTracker.getGhost());	
				System.out.print(" || DOES PATH HAVE JUNCTION?: ");
				System.out.println(ghostTracker.doesPathContainJunction_Drawn(direction, colors[j]));
			}
			
		}else{
			System.out.println("NOT POSSIBLE MOVE");
		}
		System.out.println();
    }
    
    private void test_DirectionalDistanceToNearestJunction(Game game, MOVE direction, Color color){
    	
		System.out.print("DIRECTION: ");
		System.out.println(direction);
		
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		int[] junctionIndicies = game.getJunctionIndices();
	
		double distance = 200;
		int closestNode = game.getClosestNodeIndexFromNodeIndex_Directional(pacManIndex, junctionIndicies, direction, 200);
		
		if(closestNode != -1){
			distance = game.getShortestPathDistance_absolute(pacManIndex, closestNode, direction);
		}
		
		if(distance < 200 && distance >= 0){
			System.out.println(distance);
			GameView.addPoints(game,color,game.getShortestPath_absolute(pacManIndex, closestNode, direction));
		}else{
			System.out.println(200);
		}
    }

    private void test_DirectionalDistanceToNearestPill(Game game, MOVE direction, Color color){
    	
		System.out.print("DIRECTION: ");
		System.out.println(direction);
		
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		int[] pillsIndicies = game.getActivePillsIndices();
	
		double distance = 200;
		int closestNode = game.getClosestNodeIndexFromNodeIndex_Directional(pacManIndex, pillsIndicies, direction, 200);
		
		if(closestNode != -1){
			distance = game.getShortestPathDistance_absolute(pacManIndex, closestNode, direction);
		}
		
		if(distance < 200 && distance >= 0){
			System.out.println(distance);
			GameView.addPoints(game,color,game.getShortestPath_absolute(pacManIndex, closestNode, direction));
		}else{
			System.out.println(200);
		}
    }
    

    private void test_DirectionalDistanceToNearestPowerPill(Game game, MOVE direction, Color color){
    	
		System.out.print("DIRECTION: ");
		System.out.println(direction);
		
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		int[] pillsIndicies = game.getActivePowerPillsIndices();
	
		double distance = 200;
		int closestNode = game.getClosestNodeIndexFromNodeIndex_Directional(pacManIndex, pillsIndicies, direction, 200);
		
		if(closestNode != -1){
			distance = game.getShortestPathDistance_absolute(pacManIndex, closestNode, direction);
		}
		
		if(distance < 200 && distance >= 0){
			System.out.println(distance);
			GameView.addPoints(game,color,game.getShortestPath_absolute(pacManIndex, closestNode, direction));
		}else{
			System.out.println(200);
		}
    }
    
    private void test_isGhostApproachingFromGivenDirection(Game game, MOVE direction, GHOST ghost){
    	System.out.print("DIRECTION: ");
    	System.out.println(direction);
    	System.out.print(ghost);
    	
    	int pacManIndex=game.getPacmanCurrentNodeIndex();
    	
    	MOVE incomingDirection = game.getNextMoveTowardsTarget(pacManIndex, game.getGhostCurrentNodeIndex(ghost), DM.PATH);
		if(incomingDirection == direction){
			System.out.println(" IS approaching from the direction");
		}else{
			System.out.println(" is NOT approaching from the direction");
		}
    }
    */
}