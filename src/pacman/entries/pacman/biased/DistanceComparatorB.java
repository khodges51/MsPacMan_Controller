package pacman.entries.pacman.biased;

import java.util.Comparator;

import pacman.entries.pacman.GhostTracker;
import pacman.game.Constants.MOVE;

/**
 * This comparator will sort GhostTrackers into ascending order based on their directional
 * distance from Ms.Pac-Man to the ghost. This comparator considers edible and non edible
 * ghosts separately. 
 * 
 * @author Kurt Hodges
 * 		   kuh1@aber.ac.uk
 * 
 *
 */
public class DistanceComparatorB implements Comparator<GhostTracker>{
	
	MOVE direction;
	boolean isEdible;
	
	/**
	 * Creates a new comparator for ghost trackers
	 * @param theDirection Compares GhostTrackers based on the distance to each tracker's ghost in this direction
	 */
	public DistanceComparatorB(MOVE theDirection, boolean isEdible){
		direction = theDirection;
		this.isEdible = isEdible;
	}
	
	/**
	 * Compare the directional distance to each ghost. Closer is better.
	 */
	@Override
	public int compare(GhostTracker ghost1, GhostTracker ghost2) {
		if(ghost1.getDirectionalDistance(direction, isEdible) > ghost2.getDirectionalDistance(direction, isEdible)) return 1;
		if(ghost2.getDirectionalDistance(direction, isEdible) > ghost1.getDirectionalDistance(direction, isEdible)) return -1;
		return 0;
	}

}