package edu.mtu.simulation.parameters;

/**
 * Expose various parameters that ForestSim 
 */
public class ModelParameters {

	// Percentage of economic agents to be created;
	private double economicAgentPercentage;
	
	/**
	 * Return the interval for the economicAgentPercentage
	 */
	public Object domEconomicAgentPercentage() {
		return new sim.util.Interval(0.0, 1.0);
	}

	/**
	 * Return the interval for the ecosystemsAgentHarvestOdds
	 */
	public Object domEcosystemsAgentHarvestOdds() {
		return new sim.util.Interval(0.0, 1.0);
	}
	
	/**
	 * Get the percentage of agents, as a double, that are economic optimizers.
	 */
	public double getEconomicAgentPercentage() {
		return economicAgentPercentage;
	}
	
	/**
	 * Set the target percentage of agents, as a double, that are economic optimizers.
	 */
	public void setEconomicAgentPercentage(double value) {
		if (value >= 0.0 && value <= 1.0) {
			economicAgentPercentage = value;
		}
	}
}
