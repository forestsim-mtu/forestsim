package edu.mtu.steppables.marketplace;

import java.awt.Point;
import java.util.List;

import edu.mtu.environment.StandThinning;
import edu.mtu.steppables.ParcelAgent;

/**
 * This class is used to wrap harvest requests for the marketplace.
 */
public class HarvestRequest {

	// Common information
	private ParcelAgent agent;
	private ProcessorBase deliverTo;
	
	// Used for default (i.e. clear-cut) harvests.
	private Point[] stands;

	// Used for thinning plans.
	private boolean thinning = false;
	private List<StandThinning> plans;

	/** 
	 * Private constructor for factory methods.
	 */
	private HarvestRequest() { }
	
	/**
	 * Create a new harvest request, which is always a clear-cut.
	 * 
	 * @param agent Associated with the request.
	 * @param stands To be clear-cut.
	 * @param deliverTo Process to deliver biomass to.
	 */
	public static HarvestRequest createHarvestRequest(ParcelAgent agent, Point[] stands, ProcessorBase deliverTo) {
		HarvestRequest request = new HarvestRequest();
		request.agent = agent;
		request.stands = stands;
		request.deliverTo = deliverTo;
		return request;
	}
	
	/**
	 * Create a new harvest request, based upon the plans provided.
	 * 
	 * @param agent Associated with the request.
	 * @param plans On how the points in the parcel should be processed.
	 * @param deliverTo Process to deliver biomass to.
	 */
	public static HarvestRequest createHarvestRequest(ParcelAgent agent, List<StandThinning> plans, ProcessorBase deliverTo) {
		HarvestRequest request = new HarvestRequest();
		request.agent = agent;
		request.thinning = true;
		request.plans = plans;
		request.deliverTo = deliverTo;
		return request;
	}
			
	/**
	 * Get the agent associated with the request.
	 */
	public ParcelAgent getAgent() { return agent; }
	
	/** 
	 * Get the processor for the biomass associated with the request.
	 */
	public ProcessorBase getDeliverTo() { return deliverTo; }
	
	/**
	 * Get the thinning plans associated with the request.
	 */
	public List<StandThinning> getPlans() {
		if (thinning) { 
			return plans;
		}
		throw new IllegalAccessError("No thinning plans assoicated with request.");
	}
	
	public Point[] getStands() {
		if (!thinning) {
			return stands;
		}
		throw new IllegalAccessError("Request contains thinning plans.");
	}
	
	/**
	 * True if this is a thinning request, false otherwise.
	 */
	public boolean isThinning() { return thinning; }
}
