package edu.mtu.models;

/**
 * This class represents a single stand in a forest. It is provided to a growth
 * model after initialization with the current state of the forest, and the 
 * forest will update its model with the results returned.
 */
public class Stand {
	// Input data
	public double siteIndex;
	
	// Stand Attributes
	public double height;
	public double minimumDiameter;
	public double arithmeticMeanDiameter;
	public double quadraticMeanDiameter;
	
	// Output attributes
	public int numberOfTrees;
	public double basalArea;
	public double volume;
	public int stocking;
}
