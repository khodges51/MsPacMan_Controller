package pacman.entries.pacman;

import jneat.NNode;
import jneat.Network;
import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Game;
import pacman.game.Constants.MOVE;

/**
 * This class is where contestants put their code for Ms. Pac-Man, deciding which direction to
 * travel each game cycle. My implementation will input information into a neural network for
 * each possible direction of travel, and evaluate the output to determine which direction is most
 * suitable.
 * 
 * @author Kurt Hodges
 *		   kuh1@aber.ac.uk
 */
public abstract class PacManController extends Controller<MOVE>{
	
	//The brain of this controller
	private Network network;
	//The number of network inputs
	private int numInputs;
	
	/**
	 * Create a new controller for Ms. Pac-Man
	 * @param network The neural network that the controller will use to make decisions
	 * @param numberOfInputs The number of inputs the networks has
	 */
	public PacManController(Network network, int numberOfInputs){
		this.network = network;
		numInputs = numberOfInputs;
	}
	
	/**
	 * This is the only method that needs to be implemented by contestants, it simply returns
	 * the direction to move this game cycle. 
	 */
	public MOVE getMove(Game game, long timeDue) 
	{			
		double[] networkInputs = new double[numInputs];	
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
			 
			//consoleOut_inputsAndOutputs(direction, networkInputs, networkOutput);
		}
		//System.out.println();
		
		return MOVE.getByIndex(bestMoveIndex);
	}
	
	/**
	 * Finds the non-directional inputs
	 * @param networkInputs The array of network inputs to populate 
	 * @param startIndex The index in the array to start populating with inputs from
	 * @param game A copy of the current game
	 * @return An updated version of the networksInputs array
	 */
	public abstract double[] getConstantInputs(double[] networkInputs, int startIndex, Game game);
	
	/**
	 * Finds the direction specific inputs 
	 * @param networkInputs The array of network inputs to populate 
	 * @param startIndex The index in the array to start populating with inputs from
	 * @param game A copy of the current game
	 * @param direction The direction to gather inputs for
	 * @return An updated version of the networksInputs array
	 */
	public abstract double[] getDirectionalInputs(double[] networkInputs, int startIndex, MOVE direction, Game game);
	
	
	/*
	 *  Loads the neural network with the given inputs and returns the output
	 */
	private double runNetwork(double[] networkInputs){
		//Input the input array into the network
		network.load_sensors(networkInputs);
		 
		 int net_depth = network.max_depth();
		 // first activate from sensor to next layer.... 
		 network.activate();
		 // next activate each layer until the last level is reached
		 for (int relax = 0; relax <= net_depth; relax++)
		 {
			 network.activate();
		 }
		 
		 //Find the output and reset the network
		 double output = ((NNode) network.getOutputs().elementAt(0)).getActivation();
		 network.flush();
		 return output;
	}
	
	/*
	 * Output the network inputs and output for a specific direction
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
	 * Returns the double array as a string
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
