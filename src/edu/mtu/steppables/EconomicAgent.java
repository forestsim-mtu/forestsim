package edu.mtu.steppables;

import edu.mtu.utilities.LandUseGeomWrapper;
import sim.engine.SimState;

@SuppressWarnings("serial")
public class EconomicAgent extends Agent {

	public EconomicAgent(LandUseGeomWrapper l) {
		super(l);
	}
	
	@Override
	public void step(SimState state) {
		setLandUse(0.0);
	}
}