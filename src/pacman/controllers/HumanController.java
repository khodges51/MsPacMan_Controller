package pacman.controllers;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.MOVE;

/*
 * Allows a human player to play the game using the arrow key of the keyboard.
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
    	/*
    	 * 
    	 *  This block of code is used to test new network inputs before implementation,
    	 *  what are the range of values?
    	 *  does the code work?
    	 * 
    	 */
    	
    	int currentNodeIndex=game.getPacmanCurrentNodeIndex();
		
		//get all active pills
		int[] activePills=game.getActivePillsIndices();
			
		//get all active power pills
		int[] activePowerPills=game.getActivePowerPillsIndices();
			
		//create a target array that includes all ACTIVE pills and power pills
		int[] targetNodeIndices=new int[activePills.length+activePowerPills.length];
			
		for(int i=0;i<activePills.length;i++)
			targetNodeIndices[i]=activePills[i];
			
		for(int i=0;i<activePowerPills.length;i++)
			targetNodeIndices[activePills.length+i]=activePowerPills[i];		

		int numInputs = 4;
		double inputs[] = new double[numInputs+1];
		inputs[numInputs] = -1.0; // Bias
		//INITIALIZE DISTANCE TO BE FARTHERST
		for(int k = 0; k < 4; k++){
			inputs[k] = 100;
		}
		
		MOVE moves[] = {MOVE.LEFT, MOVE.DOWN, MOVE.UP, MOVE.RIGHT};
		
		for(int i = 0; i < 4; i++){
			boolean shouldBreak = false;
			List<Integer> myCrapIdeaArray = new ArrayList<Integer>();// = new int[targetNodeIndices.length];
			//System.arraycopy(targetNodeIndices, 0, myCrapIdeaArray, 0, targetNodeIndices.length);
			
			for(int z = 0; z < targetNodeIndices.length; z++){
				myCrapIdeaArray.add(targetNodeIndices[z]);
			}
			
			for(int j = 0; j < myCrapIdeaArray.size(); j++){
				if(moves[i] == game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getClosestNodeIndexFromNodeIndex(currentNodeIndex,myCrapIdeaArray.stream().mapToInt(Integer::intValue).toArray(),DM.PATH),DM.PATH)){
					//inputs[i] = distToNode;
					inputs[i] = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(currentNodeIndex,myCrapIdeaArray.stream().mapToInt(Integer::intValue).toArray(),DM.PATH));
					shouldBreak = true;
				}else{
					Integer myInt = game.getClosestNodeIndexFromNodeIndex(currentNodeIndex,myCrapIdeaArray.stream().mapToInt(Integer::intValue).toArray(),DM.PATH);
					//Remove current closest node
					myCrapIdeaArray.remove(myInt);
				}
				
				if(shouldBreak){
					j += targetNodeIndices.length;
				}
			}
		}
		
		MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
		
		 double outputs[] = {0.0, 0.0, 0.0, 0.0};
		 for(int i = 0; i < 4; i++){
			 
			
				//FIND POSSIBLE DIRECTIONS
				//FOR EACH
				//CREATE ARRAYLIST OF NODES
				//DELETE NEIGHBOURING NODE IN ALL OTHER POSSIBLE DIRECTIONS
				//FIND SHORTEST DISTRANCE TO EACH GHOST
				//CHECK USING THE LINE FUNCTION
				//SHOULD PROB INITIALIZE INPUTS AS MAX DIST
				
			 	double gPos[] = {1000, 1000, 1000, 1000};
			 
			 	boolean isMovePoss = false;
			 	int f = 0;
			 	for(int h = 0; h < possibleMoves.length; h++){
			 		if(possibleMoves[h] == moves[i]){
			 			isMovePoss = true;
			 			f = h;
			 		}
			 	}
			 	
			 		if(isMovePoss){
						MOVE lastMove = MOVE.NEUTRAL;
						if(possibleMoves[f] == MOVE.UP){
							lastMove = MOVE.UP;
						}else if(possibleMoves[f] == MOVE.DOWN){
							lastMove = MOVE.DOWN;
						}else if(possibleMoves[f] == MOVE.LEFT){
							lastMove = MOVE.LEFT;
						}else if(possibleMoves[f] == MOVE.RIGHT){
							lastMove = MOVE.RIGHT;
						}
						
						//FOR EACH GHOST COLLECT DATA ABOUT THIS DIRECTION
						//THIS INFO IS THE INPUT FOR ONE ROUND OF THE ANN...
						//I MIGHT NEED TO RESTRUCTER THE ABOVE LOOP TO LOOP THROUGH ALL MOVES AND CHECK IF IT IS A POSSIBLE MOVE....
						for(GHOST ghost : GHOST.values())
						{
							if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0){
								if(ghost == GHOST.BLINKY){
									//System.out.println("Blinky");
									gPos[0] = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), lastMove);
									
									//VISUALISATION CODE
									if(lastMove == MOVE.LEFT){
										GameView.addPoints(game,Color.RED,game.getShortestPath(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(GHOST.BLINKY),MOVE.LEFT));
									}
									
								}else if(ghost == GHOST.INKY){ 
									//System.out.println("Inky");
									gPos[1] = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), lastMove);
								}else if(ghost == GHOST.PINKY){
									//System.out.println("Pinky");
									gPos[2] = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), lastMove);
								}else if(ghost == GHOST.SUE){
									//System.out.println("Sue");
									gPos[3] = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), lastMove);
								}
							}else if(game.getGhostEdibleTime(ghost)>0 && game.getGhostLairTime(ghost)==0){
								if(ghost == GHOST.BLINKY){
									//System.out.println("Blinky");
									gPos[0] = 1000;
								}else if(ghost == GHOST.INKY){
									//System.out.println("Inky");
									gPos[1] = 1000;
								}else if(ghost == GHOST.PINKY){
									//System.out.println("Pinky");
									gPos[2] = 1000;
								}else if(ghost == GHOST.SUE){
									//System.out.println("Sue");
									gPos[3] = 1000;
								}
							}
						}	
			 		}

			 if(gPos[0] > 1000)
				 System.out.println(gPos[0]);
			 if(gPos[1] > 1000)
				 System.out.println(gPos[1]);
			 if(gPos[2] > 1000)
				 System.out.println(gPos[2]);
			 if(gPos[3] > 1000)
				 System.out.println(gPos[3]);
		 }
    	
    	/*
    	 * 
    	 *  End of test code block
    	 * 
    	 */
    	
    	switch(input.getKey())
    	{
	    	case KeyEvent.VK_UP: 	return MOVE.UP;
	    	case KeyEvent.VK_RIGHT: return MOVE.RIGHT;
	    	case KeyEvent.VK_DOWN: 	return MOVE.DOWN;
	    	case KeyEvent.VK_LEFT: 	return MOVE.LEFT;
	    	default: 				return MOVE.NEUTRAL;
    	}
    }
}