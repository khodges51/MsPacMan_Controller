package pacman.entries.pacman;

import java.util.HashSet;

import pacman.game.Game;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * This class is used to query information about the game state. Its kind of like an extension to the Game
 * class as the methods here are like those you can see there.
 * 
 * @author Kurt Hodges
 * 		   kuh1@aber.ac.uk
 *
 */
public class GameQuery {
	
	//A copy of the current game
	private Game game;
	
	//For the maximum number of X is 40 steps story, how many steps should we search.
	//Originally 40, but open to tweaking to see if learning is easier
	private int stepsToSearch = 30;
	
	public GameQuery(Game theGame){
		game = theGame;
	}
	
	/**
	 * Are you within X steps of one of the target nodes? 
	 * @param currentIndex The position to start the search
	 * @param targetIndicies The indicies to check for a match, e.g. all active pills
	 * @param numSteps The number of steps to search for
	 * @return True if a match was found within X steps, false if no match found
	 */
	public boolean isXStepsAway(int currentIndex, int[] targetIndicies, int numSteps){
		boolean isXStepsAway = false;
		
		//Find the closest target
		int closestIndex = game.getClosestNodeIndexFromNodeIndex(currentIndex, targetIndicies, DM.PATH);
		
		//If there is a target
		if( closestIndex != -1){
			//Is it within the target distance?
			if(game.getShortestPathDistance(currentIndex, closestIndex) <= numSteps){
				isXStepsAway = true;
			}
		}
		
		return isXStepsAway;
	}

	/**
	 * Is the nearest junction blocked?
	 * @param startIndex The location of Ms.Pac-Man
	 * @param direction The direction to search for the nearest junction
	 * @return True if the nearest junction found is blocked by a non-edible ghost,
	 * false if the path to the nearest junction is clear.
	 */
	public boolean isNearestJunctionBlocked(int startIndex, MOVE direction){
		double maxDistance = 200; 
		
		//Find the nearest junction
		int closestNode = game.getClosestNodeIndexFromNodeIndex_directional(startIndex, game.getJunctionIndices(), direction, maxDistance);
		
		if(closestNode != -1){
			boolean isBlocked = false;
			
			//Find a path to the nearest junction
			int[] pathToJunction = game.getShortestPath_directional(startIndex, closestNode, direction);
			
			//Initialise the position of each ghost, remains a negative number if the ghost is edible
			int pinkyIndex = -50000;
			int blinkyIndex = -50000;
			int inkyIndex = -50000;
			int sueIndex = -50000;
			
			//Find the position of each non-edible ghost
			if(!game.isGhostEdible(GHOST.PINKY))
				pinkyIndex = game.getGhostCurrentNodeIndex(GHOST.PINKY);
			if(!game.isGhostEdible(GHOST.BLINKY))
				blinkyIndex = game.getGhostCurrentNodeIndex(GHOST.BLINKY);
			if(!game.isGhostEdible(GHOST.INKY))
				inkyIndex = game.getGhostCurrentNodeIndex(GHOST.INKY);
			if(!game.isGhostEdible(GHOST.SUE))
				sueIndex = game.getGhostCurrentNodeIndex(GHOST.SUE);
			
			//Loop through the path
			for(int i = 0; i < pathToJunction.length; i++){
				//Check if this position is occupied by a ghost
				if(pathToJunction[i] == pinkyIndex || pathToJunction[i] == sueIndex || pathToJunction[i] == blinkyIndex || pathToJunction[i] == inkyIndex){
					isBlocked = true;
				}
			}
			
			return isBlocked;
		}else{
			//No closest junction found...
			return false;
		}
	}
	
	/**
	 * Get the distance to the nearest target, starting the search in the given direction
	 * @param direction The direction to search
	 * @param searchStartIndex The location to start the search (e.g. Ms. Pac-Man's position)
	 * @param targetIndicies Search indices e.g. all active pills
	 * @param maxDistance The max distance of the search
	 * @return The distance to the nearest target in the given direction, or the max distance if no closer
	 * target is found.
	 */
	public double getDirectionalDistanceToNearest(MOVE direction, int searchStartIndex, int[] targetIndicies, double maxDistance){
		double distance = maxDistance;
		
		//Find the closest target node
		int closestNode = game.getClosestNodeIndexFromNodeIndex_directional(searchStartIndex, targetIndicies, direction, maxDistance);
		
		//If there is a match, find the distance to it
		if(closestNode != -1){
			distance = game.getShortestPathDistance_directional(searchStartIndex, closestNode, direction);
		}
		
		//Return the distance to closest node if it is below the maximum distance
		if(distance < maxDistance && distance >= 0){
			return distance;
		}else{
			return maxDistance;
		}
	}
	
	/**
	 * Searches in the given direction for 40 steps, checking each possible path, to find matches in 
	 * the 'targetNodeIndicies' array and return the maximum amount that can be found in 40 steps.
	 * @param direction The direction to search
	 * @param startIndex The location of Ms.Pac-Man
	 * @param targetNodeIndicies The indices to find matches for, e.g. all active pills
	 * @return The maximum number of matches found in 40 steps
	 */
	public double maxIn40Steps(MOVE direction, int startIndex, int[] targetNodeIndicies){
		//Start the search at the nearest neighbour
		startIndex = game.getNeighbour(startIndex, direction);
		
		//Create a hashset to reduce search time to O(1);
		HashSet<Integer> targetIndicies = new HashSet<Integer>();		
		for(int i = 0; i < targetNodeIndicies.length; i++){
			targetIndicies.add((Integer)targetNodeIndicies[i]);
		}
		
		return maxIn40Steps(direction, startIndex, targetIndicies, 0, 0);
	}
	
	/*
	 * Searches in the given direction for 40 steps, checking each possible path, to find matches in the 'targetNodeIndicies' array and return
	 * the maximum amount that can be found in 40 steps. E.g maximum number of pills in 40 steps
	 */
	private double maxIn40Steps(MOVE direction, int startIndex, HashSet<Integer> targetIndicies, int stepsPreviously, double matchesSoFar){
		int currentIndex = startIndex;
		//Need to track the max/best pills found in this branch and over all branches spawned from this branch
		double maxMatchesSoFar = matchesSoFar;
		
		//Find next neighbour
		for(int stepsSoFar = stepsPreviously; stepsSoFar < stepsToSearch; stepsSoFar++){
			//If current index is a match
			if(targetIndicies.contains((Integer)currentIndex)){
				matchesSoFar++;
			}
			
			//Find next neighbour
			int[] neighbours = game.getNeighbouringNodes(currentIndex, direction);
			
			//If we are at a junction
			for(int i = 1; i < neighbours.length; i++){
				int nodeIndex = neighbours[i];
				//Start a new branch for this neighbour
				double matchesInNewBranch = maxIn40Steps(game.getMoveToMakeToReachDirectNeighbour(currentIndex, nodeIndex), nodeIndex, targetIndicies, stepsSoFar + 1, matchesSoFar);
				
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
}
