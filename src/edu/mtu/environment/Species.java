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
	 * @return Total above ground biomass of the tree in kg (dry weight)
	 */
	double getAboveGroundBiomass(double dbh);
	
	/**
	 * Calculate the total stem wood biomass for the tree.
	 * 
	 * @param dbh The DBH of the tree, in cm.
	 * @return Stem wood biomass the tree in kg (dry weight)
	 */
	double getStemWoodBiomassRatio(double dbh);
			
	/**
	 * Get the annual mean DBH growth of the species.
	 * 
	 * @return The annual mean DBH growth, in cm.
	 */
	double getDbhGrowth();
	
	/**
	 * Get the height of a representative tree given the DBH.
	 * 
	 * @param dbh The DBH of the given tree, in cm.
	 * @return Approximate height of the given tree, in m.
	 */
	double getHeight(double dbh);
	
	/**
	 * Get the maximum DBH of the species.
	 * 
	 * @return The maximum DBH, in cm.
	 */
	double getMaximumDbh(); 
	
	/**
	 * Get the name of the species.
	 */
	String getName();
}
