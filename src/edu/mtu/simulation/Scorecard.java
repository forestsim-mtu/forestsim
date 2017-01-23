package edu.mtu.simulation;

/**
 * This interface is called at the end of each simulation step to build out a sustainability score card that
 * is 
 */
public interface Scorecard {
	/**
	 * Called when the model is first initialized, allows the for the creation of any relevant directories
	 * and gathering of appropriate data.
	 */
	public void processInitialization();
	
	/**
	 * Called at the end of each step, allows for a simulation to collect any information needed to generate
	 * a score card. 
	 */
	public void processTimeStep();
}
