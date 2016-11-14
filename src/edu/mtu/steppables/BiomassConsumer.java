package edu.mtu.steppables;

/**
 * A biomass consumer receives woody biomass as an input, and consumes it to generate an output. Harvesters 
 * will deliver the biomass to a consumer, but don't care about the underlying object. 
 */
public interface BiomassConsumer {
	/**
	 * Receive the quantity of biomass indicated in green tons. 
	 */
	public void receive(double biomass);
	
	/**
	 * Produce the quantity of output for one time step. 
	 */
	public double produce();
	
	/**
	 * The units of the output for simulation aggregation purposes. 
	 */
	public String productionUnits();
}
