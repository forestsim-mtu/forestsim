package edu.mtu.steppables;

import edu.mtu.utilities.LandUseGeomWrapper;
import sim.engine.SimState;

@SuppressWarnings("serial")
public class EcosystemsAgent extends Agent {

	public EcosystemsAgent(LandUseGeomWrapper l) {
		super(l);
	}

	@Override
	public void step(SimState state) {
		setLandUse(1.0);
	}
}
