package edu.mtu.environment;

/**
 * This interface abstracts the functions that are used to actually grow a 
 * given forest stand. These methods are called by the Forest class while 
 * the model is running.  
 */
public interface GrowthModel {
	/**
	 * Calculate the initial forest stands for the model.
	 */
	void calculateInitialStands();

	/**
	 * Get the species associated with the NLCD grid code
	 * 
	 * @param nlcd NLCD grid code to get the pattern for.
	 * @return The dominate species for the grid.
	 */
	Species getSpecies(int nlcd);
	
	/**
	 * Get the stocking guide list for the given NLCD grid code.
	 */
	double[][] getStockingGuide(int nlcd);
	
	/**
	 * Get the stocking guide list for the given species.
	 */
	double[][] getStockingGuide(Species species);
	
	/**
	 * Perform the growth operation for the given stand.
	 * 
	 * @param point The coordinate of the stand to grow.
	 */
	Stand growStand(Stand stand);
}
