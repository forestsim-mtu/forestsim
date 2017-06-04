package edu.mtu.steppables.marketplace;

import edu.mtu.simulation.ForestSim;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This class represents an agent in the marketplace that primarily transports woody biomass. 
 */
@SuppressWarnings("serial")
public abstract class Transporter implements MarketplaceAgent, Steppable {
	
	/**
	 * Receive the quantity of biomass indicated in green tons. 
	 */
	public abstract void receive(double biomass);
	
	/**
	 * Transport the quantity of output for one time step. 
	 * 
	 * @param state The current state of the ForestSim simulation.
	 */
	public abstract double transport(ForestSim state);
		
	/**
	 * Have the concrete implementation perform transportation operations.
	 */
	@Override
	public void step(SimState state) {
		transport((ForestSim)state);
	}
}
