package pacman.entries.pacman;

import java.util.Comparator;

import pacman.game.Game;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * 
 * This class can store information about a specific ghost
 * during the running of a simulation. 
 * 
 * @author Kurt Hodges
 * 		   kuh1@aber.ac.uk
 *
 */
public class GhostTracker{
	
	//Max directional distance to a ghost that should be considered
	private static double maxDistance = 500;
	//The currently running simulation
	private Game game;
	//The ghost to track
	private GHOST ghost;
	
	/**
	 * Creates a new GhostTracker
	 * @param theGhost
	 * 			The ghost to keep track of
	 * @param theGame
	 * 			The game the ghost is in
	 */
	public GhostTracker(GHOST theGhost, Game theGame){
		ghost = theGhost;
		game = theGame;
	}
	
	/**
	 * Gets the directional distance from Ms. Pac-Man to the ghost. This distance is the
	 * path distance, I.E. if you travelled through the maze to reach the ghost. 
	 * @param direction
	 * 		The direction from Ms. Pac-Man's perspective
	 * @return
	 * 		The distance between Ms. Pac-Man and the ghost if the ghost was to approach Ms. Pac-Man from 
	 * 		that direction. 
	 */
	public double getDirectionalDistance(MOVE direction){
		double realDistance;
		double distance = maxDistance;

		if(/*game.getGhostEdibleTime(ghost)==0 && */game.getGhostLairTime(ghost)==0 && game.isMovePossible(direction)){
			realDistance = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), direction);
			if(realDistance < maxDistance){
				distance = realDistance;
			}
		}
		
		return distance;
	}
	
	/**
	 * @return is this ghost edible currently
	 */
	public boolean getIsEdible(){
		if(game.getGhostEdibleTime(ghost)==0){
			return false;
		}else{
			return true;
		}
	}
}
