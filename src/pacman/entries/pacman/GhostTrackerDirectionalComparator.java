package pacman.entries.pacman;

import java.util.Comparator;

import pacman.game.Constants.MOVE;

/**
 * 
 * This comparator will sort GhostTrackers into ascending order based on their directional
 * distance to the GhostTracker's ghost. Direction must be provided in the constructor.
 * 
 * @author Kurt Hodges
 * 		   kuh1@aber.ac.uk
 * 
 *
 */
public class GhostTrackerDirectionalComparator implements Comparator<GhostTracker>{
	
	MOVE direction;
	
	/**
	 * Creates a new comparator for ghost trackers
	 * @param theDirection 
	 * 			Compares GhostTrackers based on the distance to each tracker's ghost in this direction
	 */
	public GhostTrackerDirectionalComparator(MOVE theDirection){
		direction = theDirection;
	}
	
	/**
	 * Compare the directional distance to each ghost. Closer is better.
	 */
	@Override
	public int compare(GhostTracker ghost1, GhostTracker ghost2) {
		if(ghost1.getDirectionalDistance(direction) > ghost2.getDirectionalDistance(direction)) return 1;
		if(ghost2.getDirectionalDistance(direction) > ghost1.getDirectionalDistance(direction)) return -1;
		return 0;
	}

}