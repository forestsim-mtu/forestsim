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
public abstract class HarvesterBase implements MarketplaceAgent, Steppable {
		
	/**
	 * Perform harvest operations.
	 * 
	 * @param state The current state of the ForestSim simulation.
	 */
	public abstract void harvest(ForestSim state);
	
	/**
	 * Log the indicated harvest request with this producer.
	 * 
	 * @param request The request to be logged.
	 */
	public abstract void requestHarvest(HarvestRequest request);
	
	/** 
	 * Log the indicate harvest / thinning request with this producer.
	 * 
	 * @param request The request to be logged.
	 */
	public abstract void requestThinning(HarvestRequest request);
	
	/**
	 * Have the concrete implementation perform the harvest.
	 */
	@Override
	public void step(SimState state) {
		harvest((ForestSim)state);
	}
}
