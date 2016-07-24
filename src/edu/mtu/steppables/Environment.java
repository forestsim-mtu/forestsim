package edu.mtu.steppables;

import edu.mtu.models.Forest;
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
	@Override
	public void step(SimState state) {
		try {
			Forest.getInstance().grow();
			Forest.getInstance().updateStocking();
			state.schedule.scheduleOnce(this);
		} catch (InterruptedException ex) {
			System.err.println("Unhandled error occred: " + ex);
		}
	}
}
