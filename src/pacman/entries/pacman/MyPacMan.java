package pacman.entries.pacman;

import java.util.PriorityQueue;

import pacman.Executor;
import pacman.controllers.Controller;
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
	
	/**
	 * Create a new controller for Ms. Pac-MAn
	 * @param network
	 * 			The neural network that the controller will use to make decisions
	 */
	public MyPacMan(Network network){
		this.network = network;
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
			
			//If the move is possible
			if(game.isMovePossible(direction)){
				networkInputs = gatherInputs(direction, game);
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
		
		return MOVE.getByIndex(bestMoveIndex);
	}
	
	/*
	 *  Returns the neural network inputs for the given direction
	 */
	private double[] gatherInputs(MOVE direction, Game game){
		double[] networkInputs = new double[Executor.netInputs]; 
		
		//GHOST DISTANCE 1st to 4th and are they edible??
		PriorityQueue<GhostTracker> orderedGhosts = new PriorityQueue<GhostTracker>(4, new GhostTrackerDirectionalComparator(direction));
		for(GHOST ghost : GHOST.values()){
			GhostTracker ghostTracker = new GhostTracker(ghost, game);
			orderedGhosts.add(ghostTracker);
		}
		for(int j = 0; j < 4; j++){
			GhostTracker ghostTracker = orderedGhosts.poll();
			networkInputs[j] = ghostTracker.getDirectionalDistance(direction);
			if(ghostTracker.getIsEdible()){
				networkInputs[j+4] = 1.0;
			}else{
				networkInputs[j+4] = 0.0;
			}			
		}
		return networkInputs;
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
		 return output;
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
	private MOVE myMove=MOVE.RIGHT;
	
	private Network controller;
	
	private static final int MIN_DISTANCE=20;	//if a ghost is this close, run away
	
	public MyPacMan(Network controller){
		this.controller = controller;
	}
	
	public MOVE getMove(Game game, long timeDue) 
	{			

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
							lastMove = MOVE.DOWN;
						}else if(possibleMoves[f] == MOVE.DOWN){
							lastMove = MOVE.UP;
						}else if(possibleMoves[f] == MOVE.LEFT){
							lastMove = MOVE.RIGHT;
						}else if(possibleMoves[f] == MOVE.RIGHT){
							lastMove = MOVE.LEFT;
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
			 
			 double inputsss[] = {inputs[i], gPos[0], gPos[1], gPos[2], gPos[3], inputs[4]};
			 
			 controller.load_sensors(inputsss);
			 
			 int net_depth = controller.max_depth();
			 // first activate from sensor to next layer....
			controller.activate();
			 // next activate each layer until the last level is reached
			 for (int relax = 0; relax <= net_depth; relax++)
			 {
			      controller.activate();
			 }
			 
			 outputs[i] = ((NNode) controller.getOutputs().elementAt(0)).getActivation();
		 }
		 
		 //Find largest output
		 int largestIndex = 0;
		 double largestVal = -1.0;
		 for(int i = 0; i < 4; i++){
			 if(outputs[i] > largestVal){
				 largestIndex = i;
				 largestVal = outputs[i];
			 }
		 }
		 
		 return moves[largestIndex];
		 

	}
	*/
}