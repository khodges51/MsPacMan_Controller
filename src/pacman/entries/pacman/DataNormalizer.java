package pacman.entries.pacman;

public class DataNormalizer {
	
	public double normalizeInput(double input, double maxVal){
		double normalizedInput = input;
		
		normalizedInput = input / maxVal;
		
		return normalizedInput;
	}
	
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
