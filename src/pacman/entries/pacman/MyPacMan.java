package pacman.entries.pacman;

import java.util.ArrayList;
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
			
			//GET NON-DIRECTIONAL INPUTS
			networkInputs = getConstantInputs(networkInputs, 0, game);
			
			//If the move is possible
			if(game.isMovePossible(direction)){
				//GET DIRECTIONAL INPUTS
				networkInputs = getDirectionalInputs(networkInputs, 2, direction, game);
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
		
		return MOVE.getByIndex(bestMoveIndex);
	}
	
	/*
	 *  Returns the neural network inputs that do'nt depend upon direction
	 */
	private double[] getConstantInputs(double[] networkInputs, int startIndex, Game game){
		//GET POPORTION OF REMAINING PILLS
		double amountPillsLeft = (double)game.getNumberOfActivePills() / (double)game.getNumberOfPills();
		networkInputs[startIndex] = amountPillsLeft;
		
		//GET POPORTION OF REMAINING Power PILLS
		double amountPowerPillsLeft = (double)game.getNumberOfActivePowerPills() / (double)game.getNumberOfPowerPills();
		networkInputs[startIndex + 1] = amountPowerPillsLeft;
		
		return networkInputs;
	}
	
	/*
	 *  Returns the neural network inputs for the given direction
	 */
	private double[] getDirectionalInputs(double[] networkInputs, int startIndex, MOVE direction, Game game){
		
		networkInputs = getGhostInfo(networkInputs, startIndex, direction, game);
		
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		
		//Get distance to closest pill
		networkInputs[startIndex + 8] = getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePillsIndices(), 100, game);
		
		//Get distance to closest power pill
		networkInputs[startIndex + 9] = getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePowerPillsIndices(), 500, game);
		
		//Get distance to closest junction
		networkInputs[startIndex + 10] = getDirectionalDistanceToNearest(direction, pacManIndex, game.getJunctionIndices(), 100, game);
		
		return networkInputs;
	}
	
	/*
	 * 
	 */
	private double[] getGhostInfo(double[] networkInputs, int startIndex, MOVE direction, Game game){
		//GHOST DISTANCE 1st to 4th and are they edible??
		PriorityQueue<GhostTracker> orderedGhosts = new PriorityQueue<GhostTracker>(4, new GhostTrackerDirectionalComparator(direction));
		for(GHOST ghost : GHOST.values()){
			GhostTracker ghostTracker = new GhostTracker(ghost, game);
			orderedGhosts.add(ghostTracker);
		}
		for(int j = startIndex; j < startIndex + 4; j++){
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
}