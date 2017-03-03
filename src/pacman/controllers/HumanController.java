package pacman.controllers;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.PriorityQueue;

import pacman.Executor;
import pacman.game.Game;
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
    	MOVE direction = MOVE.DOWN;
    	
    	//Run some tests to help verify if stories are complete
    	//test_DirectionalDistanceToGhosts(game, direction);
    	
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
}