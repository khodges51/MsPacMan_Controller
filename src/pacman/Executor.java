package pacman;

import jGraph.Structure;
import jNeatCommon.IOseq;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.Scanner;

import jneat.Genome;
import jneat.Neat;
import jneat.Network;
import jneat.Organism;
import pacman.controllers.Controller;
import pacman.controllers.HumanController;
import pacman.controllers.KeyBoardInput;
import pacman.controllers.examples.AggressiveGhosts;
import pacman.controllers.examples.Legacy;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.NearestPillPacMan;
import pacman.controllers.examples.NearestPillPacManVS;
import pacman.controllers.examples.RandomGhosts;
import pacman.controllers.examples.RandomNonRevPacMan;
import pacman.controllers.examples.RandomPacMan;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import pacman.entries.pacman.*;
import pacman.game.Game;
import pacman.game.GameView;
import static pacman.game.Constants.*;

/**
 * This class may be used to execute the game in timed or un-timed modes, with or without
 * visuals. Competitors should implement their controllers in game.entries.ghosts and 
 * game.entries.pacman respectively. The skeleton classes are already provided. The package
 * structure should not be changed (although you may create sub-packages in these packages).
 * 
 * @author joseatovar
 * 		   https://github.com/joseatovar,
 * 		   Kurt Hodges
 * 		   kuh1@aber.ac.uk
 */
@SuppressWarnings("unused")
public class Executor
{	
	//The number of input and output nodes the neural network should have
	public static int netInputs = 28;
	private static int netOutputs = 1;
	
	//The amount of lives Ms. Pac-Man should start with during training
	private int numberOfLives = 1;
	
	/**
	 * The main method. 
	 *
	 * @param args the command line arguments
	 * @author kuh1@aber.ac.uk
	 */
	public static void main(String[] args)
	{
		Executor exec=new Executor();
		
		//For testing, comment out when not needed. Runs a simulation with the modified human controlled class.
		//exec.runGameTimed(new HumanController(new KeyBoardInput()),new StarterGhosts(), true);
		
		/*
		//Test feature, load a chosen file
		EvolutionController evolution = new EvolutionController(1, netInputs, netOutputs);
		Network theNet = evolution.loadNetwork("savedGenomes\\GenChamp134_3794");
		System.out.println("Running a generation champion in a simulation");
		exec.runGameTimed(new MyPacMan(theNet),new StarterGhosts(), true);
		*/
		/*
		//Test feature, load the overall champ
		EvolutionController evolution = new EvolutionController(1, netInputs, netOutputs);
		Network theNet = evolution.loadNetwork("savedGenomes\\OverallChamp");
		System.out.println("Running the overall champion in a simulation");
		exec.runGameTimed(new MyPacMan(theNet),new StarterGhosts(), true);
		*/
		
		exec.initialise();
	}
	
	/*
	 * Asks the user some details about the experiments they want to run and 
	 * initialises the experiments. 
	 */
	private void initialise(){
		Scanner scanner = new Scanner(System.in);
		
		//Ask the user how big the population should be
		int popSize;
		System.out.println("Please input what size you want the population to be: ");
		popSize = scanner.nextInt();
		Neat.p_pop_size = popSize;
		
		//Ask the user how many generations to evolve and how many experiments to run during evaluation
		int numGenerations;
		int numExperiments;
		System.out.println("Please input the number of generations to evolve: ");
		numGenerations = scanner.nextInt();
		System.out.println("Please input the number of experiments to run per evaluation: ");
		numExperiments = scanner.nextInt();
		
		EvolutionController evolution = new EvolutionController(popSize, netInputs, netOutputs);

		runExperiment(evolution, 0, numGenerations, numExperiments);
		
		//Run the best network to show final controller performance visually
		if(evolution.getBestNetwork() != null){
			System.out.println("Running simulation with the best scoring network");
			runGameTimed(new MyPacMan(evolution.getBestNetwork()),new StarterGhosts(), true);
		}
	}
	
	/**
	 * Evolves a population of networks through X generations.
	 * 
	 * @param evolution The evolution controller
	 * @param startGeneration The generation number to start on
	 * @param numGenerations The generation number to end on
	 * @param numExperiments The amount of games to play to determine fitness, averaging the scores over that many games.
	 * @author kuh1@aber.ac.uk 
	 */
	public void runExperiment(EvolutionController evolution, int startGeneration, int numGenerations, int numExperiments){
		//Loop for each generation
		for(int generation = startGeneration; generation < numGenerations; generation++){
			
			//Loop for each organism in the population
			for(int j = 0;j < evolution.getPopulationSize();j++){
				evolution.evaluateOrganism(j, numExperiments, numberOfLives, this);
			}
			
			//Evolve the population
			evolution.evolvePopulation(generation);
		}
		
		//Save the best contender overall to a file
		String fileName = "savedGenomes\\OverallChamp";
		evolution.saveNetwork(evolution.getBestNetwork(), fileName);
		
		//Ask the user if they want to continue the experiment
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter 0 to finish the experiment, enter a number greater than 0 to continue the experiment for that many more generations");
		int input;
		input = scan.nextInt();
		if(input > 0){
			runExperiment(evolution, numGenerations, numGenerations+input, numExperiments);
		}
		scan.close();
	}
	
	/**
	 * Run a game in asynchronous mode: the game waits until a move is returned. In order to slow thing down in case
	 * the controllers return very quickly, a time limit can be used. If fasted gameplay is required, this delay
	 * should be put as 0.
	 *
	 * @param pacManController The Pac-Man controller
	 * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
	 * @param delay The delay between time-steps
	 */
	public double runGame(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,int delay)
	{
		Game game=new Game(0);

		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		while(!game.gameOver())
		{
	        game.advanceGame(pacManController.getMove(game.copy(),-1),ghostController.getMove(game.copy(),-1));
	        
	        try{Thread.sleep(delay);}catch(Exception e){}
	        
	        if(visual)
	        	gv.repaint();
		}
		
		//Make note of the games score so that controller fitness can be evaluated
 		double lastScore = game.getScore();
 		return lastScore;
	}
	
	/**
	 * Run a game in asynchronous mode: the game waits until a move is returned. In order to slow thing down in case
	 * the controllers return very quickly, a time limit can be used. If fasted gameplay is required, this delay
	 * should be put as 0. Modified to allow Ms.Pac Man to start with less or extra lives
	 *
	 * @param pacManController The Pac-Man controller
	 * @param numLives The number of lives Ms. Pac-Man should start with
	 * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
	 * @param delay The delay between time-steps
	 * @author Modified by Kurt Hodges
	 */
	public double runGame(Controller<MOVE> pacManController, int numLives, Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,int delay)
	{
		Game game=new Game(0);
		
		if(numLives > 0){
			game.setPacManLives(numLives);
		}
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		while(!game.gameOver())
		{
	        game.advanceGame(pacManController.getMove(game.copy(),-1),ghostController.getMove(game.copy(),-1));
	        
	        try{Thread.sleep(delay);}catch(Exception e){}
	        
	        if(visual)
	        	gv.repaint();
		}
		
		//Make note of the games score so that controller fitness can be evaluated
 		double lastScore = game.getScore();
 		return lastScore;
	}
	
	/**
     * Run the game with time limit (asynchronous mode). This is how it will be done in the competition. 
     * Can be played with and without visual display of game states.
     *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
	 * @param visual Indicates whether or not to use visuals
     */
    public void runGameTimed(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual)
	{
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		if(pacManController instanceof HumanController)
			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
				
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	   
	        
	        if(visual)
	        	gv.repaint();
		}
		
		pacManController.terminate();
		ghostController.terminate();
	}
	
    /**
     * Run the game in asynchronous mode but proceed as soon as both controllers replied. The time limit still applies so 
     * so the game will proceed after 40ms regardless of whether the controllers managed to calculate a turn.
     *     
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param fixedTime Whether or not to wait until 40ms are up even if both controllers already responded
	 * @param visual Indicates whether or not to use visuals
     */
    public double runGameTimedSpeedOptimised(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean fixedTime,boolean visual)
 	{
 		Game game=new Game(0);
 		
 		GameView gv=null;
 		
 		if(visual)
 			gv=new GameView(game).showGame();
 		
 		if(pacManController instanceof HumanController)
 			gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
 				
 		new Thread(pacManController).start();
 		new Thread(ghostController).start();
 		
 		while(!game.gameOver())
 		{
 			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
 			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

 			try
			{
				int waited=DELAY/INTERVAL_WAIT;
				
				for(int j=0;j<DELAY/INTERVAL_WAIT;j++)
				{
					Thread.sleep(INTERVAL_WAIT);
					
					if(pacManController.hasComputed() && ghostController.hasComputed())
					{
						waited=j;
						break;
					}
				}
				
				if(fixedTime)
					Thread.sleep(((DELAY/INTERVAL_WAIT)-waited)*INTERVAL_WAIT);
				
				game.advanceGame(pacManController.getMove(),ghostController.getMove());	
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
 	        
 	        if(visual)
 	        	gv.repaint();
 		}
 		
 		//Make note of the games score so that controller fitness can be evaluated
 		double lastScore = game.getScore();
 		
 		pacManController.terminate();
 		ghostController.terminate();
 		
 		return lastScore;
 	}
    
	/**
	 * Run a game in asynchronous mode and recorded.
	 *
     * @param pacManController The Pac-Man controller
     * @param ghostController The Ghosts controller
     * @param visual Whether to run the game with visuals
	 * @param fileName The file name of the file that saves the replay
	 */
	public void runGameTimedRecorded(Controller<MOVE> pacManController,Controller<EnumMap<GHOST,MOVE>> ghostController,boolean visual,String fileName)
	{
		StringBuilder replay=new StringBuilder();
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
		{
			gv=new GameView(game).showGame();
			
			if(pacManController instanceof HumanController)
				gv.getFrame().addKeyListener(((HumanController)pacManController).getKeyboardInput());
		}		
		
		new Thread(pacManController).start();
		new Thread(ghostController).start();
		
		while(!game.gameOver())
		{
			pacManController.update(game.copy(),System.currentTimeMillis()+DELAY);
			ghostController.update(game.copy(),System.currentTimeMillis()+DELAY);

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

	        game.advanceGame(pacManController.getMove(),ghostController.getMove());	        
	        
	        if(visual)
	        	gv.repaint();
	        
	        replay.append(game.getGameState()+"\n");
		}
		
		pacManController.terminate();
		ghostController.terminate();
		
		saveToFile(replay.toString(),fileName,false);
	}
	
	/**
	 * Replay a previously saved game.
	 *
	 * @param fileName The file name of the game to be played
	 * @param visual Indicates whether or not to use visuals
	 */
	public void replayGame(String fileName,boolean visual)
	{
		ArrayList<String> timeSteps=loadReplay(fileName);
		
		Game game=new Game(0);
		
		GameView gv=null;
		
		if(visual)
			gv=new GameView(game).showGame();
		
		for(int j=0;j<timeSteps.size();j++)
		{			
			game.setGameState(timeSteps.get(j));

			try
			{
				Thread.sleep(DELAY);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
	        if(visual)
	        	gv.repaint();
		}
	}
	
	//save file for replays
    public static void saveToFile(String data,String name,boolean append)
    {
        try 
        {
            FileOutputStream outS=new FileOutputStream(name,append);
            PrintWriter pw=new PrintWriter(outS);

            pw.println(data);
            pw.flush();
            outS.close();

        } 
        catch (IOException e)
        {
            System.out.println("Could not save data!");	
        }
    }  

    //load a replay
    private static ArrayList<String> loadReplay(String fileName)
	{
    	ArrayList<String> replay=new ArrayList<String>();
		
        try
        {         	
        	BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));	 
            String input=br.readLine();		
            
            while(input!=null)
            {
            	if(!input.equals(""))
            		replay.add(input);

            	input=br.readLine();	
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        
        return replay;
	}
}