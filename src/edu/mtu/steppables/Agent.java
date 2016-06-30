package edu.mtu.steppables;

import sim.engine.SimState;
import sim.engine.Steppable;
import edu.mtu.simulation.ForestSim;
import edu.mtu.utilities.LandUseGeomWrapper;

@SuppressWarnings("serial")
public class Agent implements Steppable {
	
	private LandUseGeomWrapper lu;
	
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
	
	public void step(SimState state) {
		ForestSim fs = (ForestSim)state;
		setLandUse(fs.random.nextDouble());
		lu.addDoubleAttribute("Land Use", getLandUse());
	}
}
