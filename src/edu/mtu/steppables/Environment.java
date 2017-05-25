package edu.mtu.steppables;

import edu.mtu.environment.Forest;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * The environment that the agents operate within.
 */
@SuppressWarnings("serial")
public class Environment implements Steppable {
	/**
	 * Update the forest with new growth and aggregate statistics.
	 */
	public void step(SimState state) {
		try {
			Forest.getInstance().grow();
			Forest.getInstance().updateStocking();
		} catch (InterruptedException ex) {
			System.err.println("Unhandled error occred: " + ex);
		}
	}
}
