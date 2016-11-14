package edu.mtu.steppables;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This class represents a biomass plant that receives the biomass in green tons
 * and generates electricity that is produced in MW.
 */
@SuppressWarnings("serial")
public class BiomassPlant implements Steppable, BiomassConsumer {

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

	@Override
	public void step(SimState arg0) {
		// TODO Auto-generated method stub
		
	}
}
