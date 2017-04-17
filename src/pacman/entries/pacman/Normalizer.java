package pacman.entries.pacman;

/**
 * This class handles normalising inputs for the neural network 
 * 
 * @author Kurt Hodges
 *
 */
public class Normalizer {
	
	/**
	 * Scales the given double to a value between 0 and 1
	 * @param value The boolean to scale
	 * @param maxVal The maximum value in the domain of the double
	 * @return A value between 0 and 1
	 */
	public double normalizeDouble(double value, double maxVal){
		double output = value / maxVal;
		return output;
	}
	
	/**
	 * Changes a boolean into a double for use in neural networks
	 * @param isTrue
	 * @return Returns 1.0 for true and 0.0 for false
	 */
	public double normalizeBoolean(Boolean isTrue){
		double isTrueNormalized;
		if(isTrue){
			isTrueNormalized = 1.0;
		}else{
			isTrueNormalized = 0.0;
		}
		return isTrueNormalized;
	}
}
