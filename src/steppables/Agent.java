package steppables;

import java.util.Random;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.geo.MasonGeometry;
import simulation.ForestSim;
import utilities.LandUseGeomWrapper;

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
		//lu.addDoubleAttribute("Land Use", getLandUse());
		//System.out.println(mg.getAttribute("OWNER"));
	}
}
