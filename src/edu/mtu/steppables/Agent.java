package edu.mtu.steppables;

import java.awt.Point;

import edu.mtu.simulation.ForestSim;
import edu.mtu.utilities.LandUseGeomWrapper;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.IntGrid2D;
import sim.util.IntBag;

@SuppressWarnings("serial")
public abstract class Agent implements Steppable {
	
	private LandUseGeomWrapper landUseWrapper;
	private Point[] coverPoints;
		
	/**
	 * Report what type of agent is being represented.
	 */
	public abstract AgentType getAgentType();
	
	/**
	 * Constructor.
	 */
	public Agent(LandUseGeomWrapper landUseWrapper) {
		this.landUseWrapper = landUseWrapper;
		coverPoints = null;
		setAgentType(getAgentType());
	}
	
	public LandUseGeomWrapper getGeometry() { return landUseWrapper; }
	
	/**
	 * Get the current land use for the agent's parcel.
	 */
	public double getLandUse() { return landUseWrapper.getLandUse(); }

	/**
	 * Grow the forest.
	 */
	protected void growth(SimState state) {
		// TODO Tie this to the FIA, but for now just use a random growth rate
		double use = getLandUse();
		if (use >= 1.0) { return; }
		double rate = state.random.nextDouble() / 10;		
		setLandUse(use + rate);
	}
	
	public void createCoverPoints(IntBag xPos, IntBag yPos) {
		coverPoints = new Point[xPos.size()];
		for(int i=0; i<coverPoints.length; i++) {
			coverPoints[i] = new Point(xPos.get(i), yPos.get(i));
		}
	}
	
	/**
	 * Harvest the forest.
	 */	
	protected Point harvest() {
		int rand = cern.jet.random.Uniform.staticNextIntFromTo(0, coverPoints.length-1);
		Point parcelPixel = coverPoints[rand];
		return parcelPixel;
	}
	
	/**
	 * Set the agent's type.
	 */
	protected void setAgentType(AgentType value) { landUseWrapper.setAgentType(value); }
	
	/**
	 * Set the land use for the agent's parcel.
	 */
	protected void setLandUse(double value) { landUseWrapper.setLandUse(value); }
	
	public void step(SimState state) {
		ForestSim fs = (ForestSim)state;
		if(coverPoints != null && coverPoints.length > 0) {
			Point p = harvest();
			((IntGrid2D)fs.coverLayer.getGrid()).set(p.x, p.y, 3);
		}
		//printPoints();
		//setLandUse(fs.random.nextDouble());
		//lu.addDoubleAttribute("Land Use", getLandUse());
		//System.out.println(mg.getAttribute("OWNER"));
	}
	
	/**
	 * Update the shape file to reflect the agent's attributes.
	 */
	public void updateShapefile() {
		landUseWrapper.updateShpaefile();
	}
}
