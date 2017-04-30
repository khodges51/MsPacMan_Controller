package pacman.entries.pacman.biased;

import static pacman.game.Constants.EDIBLE_TIME;
import static pacman.game.Constants.EDIBLE_TIME_REDUCTION;
import static pacman.game.Constants.LEVEL_RESET_REDUCTION;

import java.util.PriorityQueue;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.entries.pacman.GameQuery;
import pacman.entries.pacman.GhostTracker;
import pacman.entries.pacman.Normalizer;
import pacman.entries.pacman.PacManController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import jneat.*;

/**
 *
 * This implementation gathers all of the inputs from the original stories, except it considers the edible and non edible 
 * ghosts seperately. This info is into a neural network for each possible direction of travel, and evaluate the output to 
 * determine which direction is most suitable. This class will contain implementations of only the most basic inputs, using 
 * the GameQuery class for finding the remaining inputs.
 * 
 * @author Kurt Hodges
 *		   kuh1@aber.ac.uk
 */
public class MyPacManB extends PacManController
{
	//Used to scale data so it can be input into the ANN
	private Normalizer dataNormalizer;
	//Used to find information about the game state
	private GameQuery gameQuery;
	
	/**
	 * Create a new controller for Ms. Pac-Man
	 * @param network The neural network that the controller will use to make decisions
	 * @param numberOfInputs The number of inputs the networks has
	 */
	public MyPacManB(Network network, int numberOfInputs){
		super(network, numberOfInputs);
		dataNormalizer = new Normalizer();
	}
	
	/**
	 * {@inheritDoc}
	 *  || Returns all of the constant inputs from the original stories
	 */
	public double[] getConstantInputs(double[] networkInputs, int startIndex, Game game){
		GameQuery gameQuery = new GameQuery(game);
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		
		//Get proportion of remaining pills
		double amountPillsLeft = (double)game.getNumberOfActivePills() / (double)game.getNumberOfPills();
		networkInputs[startIndex] = amountPillsLeft;
		
		//Get proportion of remaining power pills
		double amountPowerPillsLeft = (double)game.getNumberOfActivePowerPills() / (double)game.getNumberOfPowerPills();
		networkInputs[startIndex + 1] = amountPowerPillsLeft;

		//Are we 10 steps away from a power pill?
		networkInputs[startIndex + 2] = dataNormalizer.normalizeBoolean(
				gameQuery.isXStepsAway(pacManIndex, game.getActivePowerPillsIndices(), 10));
		
		//Get the ghost related inputs
		networkInputs = getConstantGhostInfo(networkInputs, startIndex + 3, game);

		return networkInputs;
	}
	
	/**
	 * {@inheritDoc}
	 * || Returns all of the directional inputs from the original stories, except ghost information is split into
	 * edible and non-edible
	 */
	public double[] getDirectionalInputs(double[] networkInputs, int startIndex, MOVE direction, Game game){
		GameQuery gameQuery = new GameQuery(game);
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		
		//Get distance to closest pill
		networkInputs[startIndex] = dataNormalizer.normalizeDouble(
				gameQuery.getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePillsIndices(), 200), 200);
		 
		//Get distance to closest power pill
		networkInputs[startIndex + 1] = dataNormalizer.normalizeDouble(
				gameQuery.getDirectionalDistanceToNearest(direction, pacManIndex, game.getActivePowerPillsIndices(), 200), 200);
		
		//Get distance to closest junction
		networkInputs[startIndex + 2] = dataNormalizer.normalizeDouble(
				gameQuery.getDirectionalDistanceToNearest(direction, pacManIndex, game.getJunctionIndices(), 100), 100);
		
		//Is the nearest junction blocked?
		networkInputs[startIndex + 3] = dataNormalizer.normalizeBoolean(
				gameQuery.isNearestJunctionBlocked(pacManIndex, direction));
		
		//Max pills in 40 steps
		networkInputs[startIndex + 4] = dataNormalizer.normalizeDouble(gameQuery.maxIn40Steps(
				direction, pacManIndex, game.getActivePillsIndices()), 4);
		
		//Max junctions in 40 steps
		networkInputs[startIndex + 5] = dataNormalizer.normalizeDouble(gameQuery.maxIn40Steps(
				direction, pacManIndex, game.getJunctionIndices()), 4);
		
		//Get the ghost related inputs
		networkInputs = getDirectionalGhostInfo(networkInputs, startIndex + 6, direction, game);
		
		return networkInputs;
	}
	
	/*
	 * Adds inputs concerning information about all of the ghost into the network,
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
	 * Adds inputs concerning directional information about each ghost into the network. 
	 * edible and non-edible ghosts are consider seperatley. Ghosts are ordered from closest to farthest.
	 */
	private double[] getDirectionalGhostInfo(double[] networkInputs, int startIndex, MOVE direction, Game game){
		int pacManIndex=game.getPacmanCurrentNodeIndex();
		
		//Sort each non edible ghost by directional distance
		PriorityQueue<GhostTracker> orderedGhosts = new PriorityQueue<GhostTracker>(4, new DistanceComparatorB(direction, false));
		for(GHOST ghost : GHOST.values()){
			GhostTracker ghostTracker = new GhostTracker(ghost, game);
			orderedGhosts.add(ghostTracker);
		}
		
		//Loop through the ghosts from closest to farthest 
		for(int j = startIndex; j < startIndex + 4; j++){
			GhostTracker ghostTracker = orderedGhosts.poll();
			networkInputs[j] = dataNormalizer.normalizeDouble(ghostTracker.getDirectionalDistance(direction, false), 200);
			networkInputs[j + 4] = dataNormalizer.normalizeBoolean(ghostTracker.doesPathContainJunction(direction));
		}
		
		//Sort each edible ghost by directional distance
		orderedGhosts = new PriorityQueue<GhostTracker>(4, new DistanceComparatorB(direction, true));
		for(GHOST ghost : GHOST.values()){
			GhostTracker ghostTracker = new GhostTracker(ghost, game);
			orderedGhosts.add(ghostTracker);
		}
		
		//Loop through the ghosts from closest to farthest 
		for(int j = startIndex + 8; j < startIndex + 12; j++){
			GhostTracker ghostTracker = orderedGhosts.poll();
			networkInputs[j] = dataNormalizer.normalizeDouble(ghostTracker.getDirectionalDistance(direction, true), 200);
			networkInputs[j + 4] = dataNormalizer.normalizeBoolean(ghostTracker.doesPathContainJunction(direction));
		}
		
		return networkInputs;
	}
}