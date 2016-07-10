package edu.mtu.steppables;

import edu.mtu.models.Forest;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * The environment that the agents operate within.
 */
@SuppressWarnings("serial")
public class Environment implements Steppable {
	
	private Forest forest;
	
	/**
	 * Get the forest.
	 */
	public Forest getForest() { return forest; }
	
	/**
	 * Set the forest to operate in.
	 */
	public void setForest(Forest value) { forest = value; }

	/**
	 * Update the forest with new growth and aggregate statistics.
	 */
	@Override
	public void step(SimState state) {
		forest.grow();
		state.schedule.scheduleOnce(this);
	}
}
