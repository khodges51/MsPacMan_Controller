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
    		
    		/*
    		 * SPRINT 4 MOCK CODE
    		 */
    		/*
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
    		*/
    		
    		test_Direction = testDirection;
    		//MAX PILLS IN 40 Steps
    		//System.out.println("SEARCHING FOR PILLS");
    		//test_maxXIn40Steps(testDirection, pacManIndex, game.getActivePillsIndices(), game);
    		
    		//MAX JUNCTIONS IN 40 Steps
    		System.out.println("SEARCHING FOR JUNCTIONS");
    		test_maxXIn40Steps(testDirection, pacManIndex, game.getJunctionIndices(), game);
    		
    		/*
    		 * 
    		 */
    		
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
    
	private double test_maxXIn40Steps(MOVE direction, int startIndex, int[] targetNodeIndicies, Game game){
		System.out.print("DIRECTION: ");
		System.out.println(direction);
		System.out.print("Max found in 40 steps: ");
		
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
		System.out.println(maxMatchesSoFar);
		
		return maxMatchesSoFar;
	}
	
	private double test_maxXIn40Steps(MOVE direction, int startIndex, int[] targetNodeIndicies, int stepsPreviously, double matchesSoFar, Game game){
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
    
    /*
     * Paints different coloured lines to each ghost in the given direction and outputs
     * the distance to them via the console.
     */
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
			
		}else/*Else if the move isn't possible*/{
			System.out.println("NOT POSSIBLE MOVE");
		}
		System.out.println();
    }
    
    /*
     * Paints different coloured lines to each ghost in the given direction and outputs
     * the distance to them via the console.
     */
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
			
		}else/*Else if the move isn't possible*/{
			System.out.println("NOT POSSIBLE MOVE");
		}
		System.out.println();
    }
    
    /*
     * 
     */
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
    
    /*
     * 
     */
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
    
    /*
     * 
     */
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
}