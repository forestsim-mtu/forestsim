package edu.mtu.steppables;

import java.awt.Point;

import ec.util.MersenneTwisterFast;
import edu.mtu.environment.Forest;
import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.ForestSim;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.IntBag;

/**
 * This class represents the base class for all of the parcel agents in ForestSim. 
 * It was originally written with the assumption that they would be NIPF owners, but
 * can be expanded beyond that.
 */
@SuppressWarnings("serial")
public abstract class ParcelAgent implements Steppable {
	private ParcelAgentType type;
	private LandUseGeomWrapper landUseWrapper;
	private MersenneTwisterFast random;
	private Point[] parcel;
	
	protected ForestSim state;

	/**
	 * Inform the agent that their parcel was harvested.
	 */
	public abstract void doHarvestedOperation();
	
	/**
	 * Have the agent perform operations that are related to the policy being investigated.
	 */
	protected abstract void doPolicyOperation();
	
	/**
	 * Have the agent perform operations that are related to harvesting.
	 */
	protected abstract void doHarvestOperation();
	
	/**
	 * Constructor.
	 */
	public ParcelAgent(ParcelAgentType type) {
		this.landUseWrapper = new LandUseGeomWrapper();
		this.landUseWrapper.setAgentType(type);
		parcel = null;
	}
		
	/**
	 * Report what type of agent is being represented.
	 */
	public ParcelAgentType getAgentType() { return type; }

	/**
	 * Get the cover points that this agent is responsible for.
	 */
	public Point[] getParcel() { return parcel; }
	
	/**
	 * Get the geometry that this agent is responsible for.
	 */
	public LandUseGeomWrapper getGeometry() { return landUseWrapper; }
	
	/**
	 * Get the current land use for the agent's parcel.
	 */
	public double getLandUse() { return landUseWrapper.getLandUse(); }
	
	
	/**
	 * Get the area, in acres, of the parcel that the agent owns.
	 */
	public double getParcelArea() {
		return parcel.length * Forest.getInstance().getAcresPerPixel();
	}
		
	/**
	 * Set the land use wrapper to be used by this agent.
	 */
	public void setLandUseWrapper(LandUseGeomWrapper landUseWrapper) {
		this.landUseWrapper = landUseWrapper;
	}
	
	/**
	 * Set the random number generator to be used by this agent.
	 */
	public void setRandom(MersenneTwisterFast random) {
		this.random = random;
	}
	
	/**
	 * Add the given points to the agents for the parcel that it controls.
	 * 
	 * @param xPos The x positions.
	 * @param yPos The y positions.
	 */
	public void createCoverPoints(IntBag xPos, IntBag yPos) {
		parcel = new Point[xPos.size()];
		for(int i=0; i<parcel.length; i++) {
			parcel[i] = new Point(xPos.get(i), yPos.get(i));
		}
	}
	
	/**
	 * Allow the agent to perform the rules for the given state.
	 */
	public void step(SimState state) {
		this.state = (ForestSim)state;
		
		PolicyBase policy = ((ForestSim)state).getPolicy();
		if (policy != null && policy.isIntroduced()) {
			doPolicyOperation();
		}
		doHarvestOperation();
	}
		
	/**
	 * Get the random number generator associated with this agent.
	 */
	protected MersenneTwisterFast getRandom() { return random; }
	
	/**
	 * Set the land use for the agent's parcel.
	 */
	protected void setLandUse(double value) { landUseWrapper.setLandUse(value); }
		
	/**
	 * Update the shape file to reflect the agent's attributes.
	 */
	protected void updateShapefile() {
		landUseWrapper.updateShpaefile();
	}
}
