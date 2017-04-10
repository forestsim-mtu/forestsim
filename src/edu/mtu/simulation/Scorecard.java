package edu.mtu.simulation;

/**
 * This interface is called at the end of each simulation step to build out a score card that is based
 * upon the model state. 
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
	
	/**
	 * Called at the end of the simulation, allows for the score card to clean up before shutdown.
	 */
	public void processFinalization();
}
