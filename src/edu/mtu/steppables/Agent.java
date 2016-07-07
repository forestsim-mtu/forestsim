package edu.mtu.steppables;

import sim.engine.SimState;
import sim.engine.Steppable;
import edu.mtu.simulation.ForestSim;
import edu.mtu.utilities.LandUseGeomWrapper;

// TODO Impliment an EconomicAgent and an EcosystemsAgent that are responsible for performing the actual step actions
// TODO Impliment a factory that is responsible for creating agents based upon the probablity passed in
@SuppressWarnings("serial")
public abstract class Agent implements Steppable {
	
	private LandUseGeomWrapper lu;
	
	public abstract void step(SimState state);
	
	public Agent(LandUseGeomWrapper l) {
		lu = l;
	}
	
	private void setLandUse(double d) {
		lu.setLandUse(d);
	}
	
	private double getLandUse() {
		return lu.getLandUse();
	}
	
	public void updateShapefile() {
		lu.updateShpaefile();
	}
}
