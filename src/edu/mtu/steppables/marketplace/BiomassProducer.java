package edu.mtu.steppables.marketplace;

import edu.mtu.simulation.ForestSim;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * A biomass producer receives harvest requests as an input, and then completes 
 * the indicated operation up to their ability. They will deliver the will deliver
 * the biomass to a consumer, but don't care about the underlying object.  
 */
@SuppressWarnings("serial")
public abstract class BiomassProducer implements Steppable {
	/**
	 * Get the capacity supported by this producer.
	 */
	public abstract double getCapacity();
		
	/**
	 * Perform harvest operations.
	 * 
	 * @param state The current state of the ForestSim simulation.
	 */
	public abstract void harvest(ForestSim state);
	
	/**
	 * The units of the output for simulation aggregation purposes. 
	 */
	public abstract String productionUnits();
	
	/**
	 * Log the indicated harvest request with this producer.
	 * 
	 * @param request The request to be logged.
	 */
	public abstract void requestHarvest(HarvestRequest request);
	
	/**
	 * Have the concrete implementation perform the harvest, then restore the agent to the schedule.
	 */
	public void step(SimState state) {
		harvest((ForestSim)state);
		state.schedule.scheduleOnce(this);
	}
}
