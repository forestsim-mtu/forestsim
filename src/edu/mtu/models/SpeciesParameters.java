package edu.mtu.models;

/**
 * This enumeration wraps the information that is needed to to work with the various tree species.
 * 
 * Note that the b1, b2, and b3 parameters are for Chapman-Richards function as described here: http://www.nrs.fs.fed.us/pubs/gtr/gtr-p-24%20papers/39kershaw-p-24.pdf
 */
public enum SpeciesParameters {
	PinusStrobus("Eastern White Pine", 102.0, 48.0, 49.071, 0.016, 1);
	
	private String name;
	private double maximumDbh;
	private double maximumHeight;
	
	// Exposed due to their use in equations
	public double b1, b2, b3;
	
	private SpeciesParameters(String name, double maximumDbh, double maximumHeight, double b1, double b2, double b3) {
		this.name = name;
		this.maximumDbh = maximumDbh;
		this.maximumHeight = maximumHeight;
		this.b1 = b1;
		this.b2 = b2;
		this.b3 = b3;
	}
	
	/**
	 * Returns the common name of the species.
	 */
	public String getName() { return name; }
	
	/**
	 * Returns the maximum DBH in centimeters.
	 */
	public double getMaximumDbh() { return maximumDbh; }
	
	/**
	 * Returns the maximum height in meters.
	 */
	public double getMaximumHeight() { return maximumHeight; }
	
	@Override
	public String toString() { return name;	}
}
