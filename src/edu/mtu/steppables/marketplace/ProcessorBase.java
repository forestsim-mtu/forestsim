package edu.mtu.steppables.marketplace;

import edu.mtu.simulation.ForestSim;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * A biomass consumer receives woody biomass as an input, and consumes it to generate an output. Harvesters 
 * will deliver the biomass to a consumer, but don't care about the underlying object. 
 */
@SuppressWarnings("serial")
public abstract class ProcessorBase implements MarketplaceAgent, Steppable {
	
	/**
	 * Receive the quantity of biomass indicated in green tons. 
	 */
	public abstract void receive(double biomass);
	
	/**
	 * Produce the quantity of output for one time step. 
	 * 
	 * @param state The current state of the ForestSim simulation.
	 * @return The quantity of the product produced.
	 */
	public abstract double produce(ForestSim state);
	
	/**
	 * Have the concrete implementation perform production operations.
	 */
	@Override
	public void step(SimState state) {
		produce((ForestSim)state);
	}
}
