package edu.mtu.steppables;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This agent acts as the central point of collection of biomass and reports statistics back accordingly.
 */
@SuppressWarnings("serial")
public class Biorefinary implements Steppable, BiomassConsumer {
	/**
	 * 
	 */
	@Override
	public void step(SimState state) {
		// TODO Auto-generated method stub
	}

	@Override
	public void receive(double biomass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double produce() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String productionUnits() {
		// TODO Auto-generated method stub
		return null;
	}
}
