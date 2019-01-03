package edu.mtu.environment;

/**
 * This enumeration provides some very rough values for various stand 
 * thinning types, in general modelers will need to provide their own
 * values based upon their regional characteristics.
 */
public enum StandThinningType {

	/**
	 * For a clear cut, all trees in the stand will be harvested. 
	 */
	ClearCut("Clear Cut", 1.0),
	
	/**
	 * Seed tree thinning leaves only a select few trees behind in the stand.
	 * 
	 * Value assumes a mature stand with 150 trees per acre with six left behind for seeds.
	 */
	SeedTree("Seed Tree", 0.96),
	
	/**
	 * Shelterwood leaves larger blocks of trees behind, but most are harvested.
	 * 
	 * Value is a rule of thumb since we assume a even-aged stand.
	 */
	Shelterwood("Shelterwood", 0.75),
	
	/**
	 * Selective harvest only removes the highest quality trees, this quantify can be highly variable.
	 */
	SelectiveHarvest("Selective Harvest", 0.3);
	
	private final double percentage;
	private final String label;
		
	private StandThinningType(String label, double percentage) {
		this.label = label;
		this.percentage = percentage;
	}
	
	/**
	 * Get the percentage of trees in the stand that should be harvested.
	 */
	public double getPercentage() {
		return percentage;
	}
	
	/**
	 * Overridden, returns type of thinning.
	 */
	@Override
	public String toString() {
		return label;
	}
}
