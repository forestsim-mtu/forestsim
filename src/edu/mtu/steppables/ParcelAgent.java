package edu.mtu.steppables;

import java.awt.Point;

import edu.mtu.environment.Forest;
import edu.mtu.measures.ForestMeasures;
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
	
	private ParcelAgentType parcelAgentType = null;
	
	private boolean hasRun = false;
	private LandUseGeomWrapper landUseWrapper;
	private Point[] parcel = null;
	
	// Land Tenure attributes, note the defaults assume immediate tenure
	private boolean phasedIn = true;
	private double phaseInRate = 1.0;
	
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
	public ParcelAgent(ParcelAgentType type, LandUseGeomWrapper lu) {
		parcelAgentType = type;
		landUseWrapper = lu;
		landUseWrapper.setAgentType(type);
	}
		
	/**
	 * Report what type of agent is being represented.
	 */
	public ParcelAgentType getAgentType() { return parcelAgentType; }

	/**
	 * Get the cover points that this agent is responsible for.
	 */
	public Point[] getParcel() { return parcel; }
	
	/**
	 * Get the agent land tenure phase-in rate.
	 */
	public double getPhaseInRate() {
		return phaseInRate;
	}
	
	/**
	 * Get the geometry that this agent is responsible for.
	 */
	public LandUseGeomWrapper getGeometry() { return landUseWrapper; }
	
	/**
	 * Get the area, in acres, of the parcel that the agent owns.
	 */
	public double getParcelArea() {
		return parcel.length * Forest.getInstance().getAcresPerPixel();
	}
	
	/**
	 * Returns true if the agent has been phased into the model, false otherwise.
	 */
	public boolean phasedIn() {
		return phasedIn;
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
	 * Set the agent land tenure phase-in rate and flag the agent has not being phased in.
	 * 
	 * NOTE: This operation must be done prior to the first step.
	 */
	public void setPhaseInRate(double value) {
		if (hasRun) {
			throw new IllegalAccessError("Land Tenure phase-in cannot be set once the agent steps!");
		}
		phaseInRate = value;
		phasedIn = false;
	}
	
	/**
	 * Allow the agent to perform the rules for the given state.
	 */
	public void step(SimState state) {
		// Accounting flag to disable operations once the model starts
		hasRun = true;
		
		// To account for land tenure, phase agents into the model at the given rate
		if (!phasedIn) {
			if (phaseInRate < state.random.nextDouble()) {
				return;
			}
			phasedIn = true;
		}
		
		this.state = (ForestSim)state;
		doPolicyOperation();
		doHarvestOperation();
	}
		
	/**
	 * Update the shape file to reflect the agent's attributes.
	 */
	public void updateShapefile() {
		double value = ForestMeasures.calculateParcelAge(parcel);
		landUseWrapper.setAverageForestAge(value);
		
		value = ForestMeasures.calculateParcelDbh(parcel);
		landUseWrapper.setAverageForestDbh(value);
		
		value = ForestMeasures.calculateParcelStocking(parcel);
		landUseWrapper.setAverageForestStocking(value);
		
		landUseWrapper.updateShpaefile();
	}
}
