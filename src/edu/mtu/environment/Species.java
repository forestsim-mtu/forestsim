package edu.mtu.environment;

/**
 * This interface defines the methods that need to be implemented for supported 
 * tree species.
 */
public interface Species {
	/**
	 * Calculate the total above ground biomass for a tree.
	 * 
	 * @param dbh The DBH of the tree, in cm.
	 * @return Total above ground biomass of the tree in kg dry weight
	 */
	double getBiomass(double dbh);
		
	/**
	 * Get the height of a representative tree given the DBH.
	 * 
	 * @param dbh The DBH of the given tree, in cm.
	 * @return Approximate height of the given tree, in m.
	 */
	double getHeight(double dbh);
	
	/**
	 * Get the name of the species.
	 */
	String getName();
}
