package edu.mtu.models.growthmodels;

import java.awt.Point;
import java.util.List;

import edu.mtu.models.Stand;

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
	 * Get the growth pattern for the NLCD grid code.
	 * 
	 * @param nlcd NLCD grid code to get the pattern for.
	 * @return The growth pattern to use when growing the forest.
	 */
	SpeciesParameters getGrowthPattern(int nlcd);
	
	/**
	 * Get the stocking guide list for the given NLCD grid code.
	 */
	List<double[]> getStockingGuide(int nlcd);
	
	/**
	 * Get the stocking guide list for the given species.
	 */
	List<double[]> getStockingGuide(SpeciesParameters species);
	
	/**
	 * Perform the growth operation for the given stand.
	 * 
	 * @param point The coordinate of the stand to grow.
	 */
	Stand growStand(Stand stand);
}
