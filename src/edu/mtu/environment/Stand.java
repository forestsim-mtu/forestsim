package edu.mtu.environment;

import java.awt.Point;

/**
 * This class represents a single stand in a forest. It is provided to a growth
 * model after initialization with the current state of the forest, and the 
 * forest will update its model with the results returned.
 */
public class Stand {
	// Bookkeeping data
	public Point point;
	
	// Input data
	public double siteIndex;
	
	// Stand Attributes
	public double minimumDiameter;
	public double arithmeticMeanDiameter;
	public double quadraticMeanDiameter;
	public int nlcd;
	public Species dominateSpecies;
	
	// Output attributes
	public int numberOfTrees;
	public double basalArea;
	public double volume;
	public int stocking;
	public int age;
}
