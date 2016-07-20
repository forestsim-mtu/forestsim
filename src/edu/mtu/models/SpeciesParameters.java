package edu.mtu.models;

/**
 * This enumeration wraps the information that is needed to to work with the various tree species. 
 * Note that the growth patterns assume ideal conditions and silviculture management.
 * 
 * Note that the b1, b2, and b3 parameters are for Chapman-Richards function as described here: http://www.nrs.fs.fed.us/pubs/gtr/gtr-p-24%20papers/39kershaw-p-24.pdf
 */
public enum SpeciesParameters {
	// https://www.na.fs.fed.us/pubs/silvics_manual/volume_2/acer/rubrum.htm
	// http://dnr.wi.gov/topic/ForestManagement/documents/24315/51.pdf
	AcerRubrum("Red Maple", "data/AcerRebrum.csv", 495.0, 38.1, 0.57, 1.0, 29.007, 0.053, 1.175),			// Height growth is a guess
	
	// https://www.na.fs.fed.us/spfo/pubs/silvics_manual/Volume_1/pinus/strobus.htm
	// http://dnr.wi.gov/topic/ForestManagement/documents/24315/31.pdf
	PinusStrobus("Eastern White Pine", "data/PinusStrobus.csv", 102.0, 48.0, 0.5, 1.0, 49.071, 0.016, 1);
	
	private String dataFile;
	private String name;
	private double dbhGrowth;
	private double maximumDbh;
	private double maximumHeight;
	private double heightGrowth;
	
	// Exposed due to their use in equations
	public double b1, b2, b3;
	
	private SpeciesParameters(String name, String dataFile, double maximumDbh, double maximumHeight, double dbhGrowth, double heightGrowth, double b1, double b2, double b3) {
		this.name = name;
		this.dataFile = dataFile;
		this.maximumDbh = maximumDbh;
		this.maximumHeight = maximumHeight;
		this.dbhGrowth = dbhGrowth;
		this.heightGrowth = heightGrowth;
		this.b1 = b1;
		this.b2 = b2;
		this.b3 = b3;
	}
	
	/**
	 * Returns the data file to use for the species.
	 */
	public String getDataFile() { return dataFile; }
	
	/**
	 * Returns the common name of the species.
	 */
	public String getName() { return name; }
	
	/**
	 * Get the maximum annual DBH growth.
	 */
	public double getDbhGrowth() { return dbhGrowth; }
	
	/**
	 * Get the maximum annual height growth.
	 */
	public double getHeightGrowth() { return heightGrowth; }
	
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
