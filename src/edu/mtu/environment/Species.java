package edu.mtu.environment;

/**
 * This interface defines the methods that need to be implemented for supported 
 * tree species.
 */
public interface Species {
	/**
	 * Calculate the total above ground biomass for a tree given the height and DBH.
	 * 
	 * @param dbh The DBH of the tree in meters.
	 * @param height The height of the tree in meters.
	 * @return The total biomass of the tree in kg of dry weight
	 */
	double getBiomass(double dbh, double height);
	
	/**
	 * Get the height of a representative tree given the DBH.
	 * 
	 * @param dbh The DBH of the given tree.
	 * @return Approximate height of the given tree, in cm.
	 */
	double getHeight(double dbh);
	
	/**
	 * Get the name of the species.
	 */
	String getName();
}
