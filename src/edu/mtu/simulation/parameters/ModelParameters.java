package edu.mtu.simulation.parameters;

/**
 * Expose various parameters that ForestSim 
 */
public class ModelParameters {

	private int finalTimeStep;					// Time step that the simulation is allowed to run to
	private int policyActivationTimeStep;		// Time step at which the policy is introduced
	private double economicAgentPercentage;		// Percentage of economic agents to be created
		
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
	 * Get the time step at which the simulation should stop.
	 */
	public int getFinalTimeStep() {
		return finalTimeStep;
	}
	
	/**
	 * Get the time step at which the policy should be introduced.
	 */
	public int policyActiviationStep() {
		return policyActivationTimeStep;
	}
	
	/**
	 * Set the target percentage of agents, as a double, that are economic optimizers.
	 */
	public void setEconomicAgentPercentage(double value) {
		if (value >= 0.0 && value <= 1.0) {
			economicAgentPercentage = value;
		}
	}
	
	/**
	 * Set the the time step that the simulation should stop at the end of.
	 */
	public void setFinalTimeStep(int value) {
		finalTimeStep = value;
	}
	
	/**
	 * Set the time step at which the policy should be introduced.
	 */
	public void setPolicyActiviationStep(int value) {
		policyActivationTimeStep = value;
	}
}
