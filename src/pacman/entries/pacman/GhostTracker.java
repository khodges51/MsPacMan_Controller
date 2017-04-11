package pacman.entries.pacman;

import java.awt.Color;
import pacman.game.Game;
import pacman.game.GameView;
import pacman.game.Constants.DM;
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
	private static double maxDistance = 200;
	//The currently running simulation
	private Game game;
	//The ghost to track
	private GHOST ghost;
	
	/**
	 * Creates a new GhostTracker
	 * @param theGhost The ghost to keep track of
	 * @param theGame The game the ghost is in
	 */
	public GhostTracker(GHOST theGhost, Game theGame){
		ghost = theGhost;
		game = theGame;
	}
	
	/**
	 * Gets the directional distance from Ms. Pac-Man to the ghost. This distance is the
	 * path distance, I.E. if you travelled through the maze to reach the ghost. 
	 * @param direction The direction from Ms. Pac-Man's perspective
	 * @return The distance between Ms. Pac-Man and the ghost if the ghost was to approach Ms. Pac-Man from 
	 * that direction. 
	 */
	public double getDirectionalDistance(MOVE direction){
		double realDistance;
		double distance = maxDistance;

		//Only compute if the ghost is out of the lair and the move is possible
		if(game.getGhostLairTime(ghost)==0 && game.isMovePossible(direction)){
			realDistance = game.getShortestPathDistance_absolute(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), direction);
			if(realDistance < maxDistance){
				distance = realDistance;
			}
		}
		
		return distance;
	}
	
	/**
	 * Checks if the path from Ms. Pac-Man in the given direction to this ghost contains a junction or not.
	 * @param direction
	 * @return 1.0 if there is a junction, 0.0 if not
	 */
	public boolean doesPathContainJunction(MOVE direction){
		int[] shortestPath = new int[0];
		
		//Only compute if the ghost is out of the lair and the move is possible
		if(game.getGhostLairTime(ghost)==0 && game.isMovePossible(direction)){
			shortestPath = game.getShortestPath_absolute(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), direction);
		}else{
			//Return true if the ghost is in the lair
			return true;
		}
		
		//Search for a junction in the path array
		for(int i = 0; i < shortestPath.length; i++){
			if(game.isJunction(shortestPath[i]))
					return true;
		}
		
		return false;
	}
	
	/**
	 * Is the ghost approaching from this direction?
	 * This is a simplified interpretation of the story. I could possibly take into account ghosts last direction of 
	 * travel/non-reversal rule to come up with a more comprehensive implementation. 
	 * @param direction The direction to check "is the ghost approaching from this direction" from Ms. Pac-Man's perspective
	 * @param pacManIndex The current location of Ms. Pac-Man
	 * @return True if the ghost is approaching, false if not
	 */
	public boolean isGhostApproaching(MOVE direction, int pacManIndex){
		//Find the direction you would travel from Ms.Pac-Man to the ghost. This is probably the optimal path and so the direction
		//of approach.
		MOVE incomingDirection = game.getNextMoveTowardsTarget(pacManIndex, game.getGhostCurrentNodeIndex(ghost), DM.PATH);
		if(incomingDirection == direction){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * @return 1.0 if ghost is edible or 0.0 if ghost is not
	 */
	public boolean isEdible(){
		if(game.getGhostEdibleTime(ghost)==0){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 
	 * @return The ghost this tracker is tracking
	 */
	public GHOST getGhost(){
		return ghost;
	}
	
	
	/*
	 * TEST STUFF
	 */
	
	
	/**
	 * This is a test version of the function, it draws a coloured line to the ghost from
	 * Ms Pac.Man
	 * 
	 * Gets the directional distance from Ms. Pac-Man to the ghost. This distance is the
	 * path distance, I.E. if you travelled through the maze to reach the ghost. 
	 * @param direction The direction from Ms. Pac-Man's perspective
	 * @return The distance between Ms. Pac-Man and the ghost if the ghost was to approach Ms. Pac-Man from 
	 * that direction. 
	 */
	/*
	public double getDirectionalDistance_drawn(MOVE direction, Color color){
		double realDistance;
		double distance = maxDistance;

		if(game.getGhostLairTime(ghost)==0 && game.isMovePossible(direction)){
			realDistance = game.getShortestPathDistance_absolute(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), direction);
			GameView.addPoints(game,color,game.getShortestPath_absolute(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), direction));
			if(realDistance < maxDistance){
				distance = realDistance;
			}
		}
		
		return distance;
	}
	*/
	/**
	 * 
	 * @param direction
	 * @param color
	 * @return
	 */
	/*
	public double doesPathContainJunction_Drawn(MOVE direction, Color color){
		double doesContainJunction = -1.0;
		int[] shortestPath = new int[0];
		
		if(game.getGhostLairTime(ghost)==0 && game.isMovePossible(direction)){
			shortestPath = game.getShortestPath_absolute(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), direction);
			GameView.addPoints(game,Color.GRAY,shortestPath);
		}
		
		for(int i = 0; i < shortestPath.length; i++){
			if(game.isJunction(shortestPath[i])){
					doesContainJunction = 1.0;
					GameView.addPoints(game, color, shortestPath[i]);
			}
		}
		
		if(game.getGhostLairTime(ghost)!=0){
			doesContainJunction = 1.0;
		}
		
		return doesContainJunction;
	}
	*/
}
