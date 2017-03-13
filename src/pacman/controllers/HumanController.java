package pacman.controllers;

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
    	MOVE testDirection = MOVE.DOWN;
    	
    	//double amountPowerPillsLeft = (double)game.getNumberOfActivePowerPills() / (double)game.getNumberOfPowerPills();
		//System.out.println(amountPowerPillsLeft);
    	
    	//double amountPillsLeft = (double)game.getNumberOfActivePills() / (double)game.getNumberOfPills();
    	//System.out.println(amountPillsLeft);
    	
    	//Direction specific tests
    	if(game.isMovePossible(testDirection)){
    		//Run some tests to help verify if stories are complete
        	//test_DirectionalDistanceToGhosts(game, testDirection);
    		//test_DirectionalDistanceToNearestPill(game, testDirection, Color.red);
    		//test_DirectionalDistanceToNearestPowerPill(game, testDirection, Color.red);
    		//test_DirectionalDistanceToNearestJunction(game, testDirection, Color.red);
    	}
    	
    	Color[] colors = {Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN};
    	//Tests for all 4 directions
    	for(int i = 0; i < 4; i++){
    		testDirection = MOVE.getByIndex(i);
    		if(game.isMovePossible(testDirection)){
            	//test_DirectionalDistanceToNearestPill(game, testDirection, colors[i]);
        		//test_DirectionalDistanceToNearestPowerPill(game, testDirection, colors[i]);
        		test_DirectionalDistanceToNearestJunction(game, testDirection, colors[i]);
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
     * 
     */
    private void test_DirectionalDistanceToNearestJunction(Game game, MOVE direction, Color color){
    	
		System.out.print("DIRECTION: ");
		System.out.println(direction);
		
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		int[] junctionIndicies = game.getJunctionIndices();
	
		double distance = 100;
		int closestNode = game.getClosestNodeIndexFromNodeIndex_Directional(pacManIndex, junctionIndicies, direction, 100);
		
		if(closestNode != -1){
			distance = game.getShortestPathDistance_absolute(pacManIndex, closestNode, direction);
		}
		
		if(distance < 100 && distance >= 0){
			System.out.println(distance);
			GameView.addPoints(game,color,game.getShortestPath_absolute(pacManIndex, closestNode, direction));
		}else{
			System.out.println(100);
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
	
		double distance = 100;
		int closestNode = game.getClosestNodeIndexFromNodeIndex_Directional(pacManIndex, pillsIndicies, direction, 100);
		
		if(closestNode != -1){
			distance = game.getShortestPathDistance_absolute(pacManIndex, closestNode, direction);
		}
		
		if(distance < 100 && distance >= 0){
			System.out.println(distance);
			GameView.addPoints(game,color,game.getShortestPath_absolute(pacManIndex, closestNode, direction));
		}else{
			System.out.println(100);
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
	
		double distance = 500;
		int closestNode = game.getClosestNodeIndexFromNodeIndex_Directional(pacManIndex, pillsIndicies, direction, 500);
		
		if(closestNode != -1){
			distance = game.getShortestPathDistance_absolute(pacManIndex, closestNode, direction);
		}
		
		if(distance < 500 && distance >= 0){
			System.out.println(distance);
			GameView.addPoints(game,color,game.getShortestPath_absolute(pacManIndex, closestNode, direction));
		}else{
			System.out.println(100);
		}
    }
}