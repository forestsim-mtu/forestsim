package edu.mtu.simulation.parameters;

/**
 * Expose various basic parameters that ForestSim uses. 
 */
public class ParameterBase {

	// Display width and height
	private int gridWidth = 1000;
	private int gridHeight = 900;
	
	// Flag to indicate if warnings should be treated as errors
	private boolean warningsAsErrors = false;
	
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
	 * Get the height of the UI grid.
	 */
	public int getGridHeight() { return gridHeight; }
	
	/**
	 * Get the width of the UI grid.
	 */
	public int getGridWidth() { return gridWidth; }
	
	/**
	 * Get the flag that indicates warnings should be treated as errors.
	 */
	public boolean getWarningsAsErrors() { return warningsAsErrors; }
	
	/**
	 * Get the time step at which the policy should be introduced.
	 */
	public int getPolicyActiviationStep() {
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
	
	/**
	 * Set the height of the UI grid.
	 */
	public void setGridHeight(int value) { gridHeight = value; }
	
	/**
	 * Set the width of the UI grid.
	 */
	public void setGridWidth(int value) { gridWidth = value; }
	
	/**
	 * Set the flag to treat warnings as errors.
	 */
	public void setWarningsAsErrors(boolean value) { warningsAsErrors = value; }
}
