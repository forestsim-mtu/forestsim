package edu.mtu.steppables.marketplace;

public interface MarketplaceAgent {
	/**
	 * Get the capacity supported by this producer.
	 */
	public double getCapacity();

	/**
	 * The units of the output for simulation aggregation purposes. 
	 */
	public String productionUnits();
}
