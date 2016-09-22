package edu.mtu.steppables;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import edu.mtu.management.HarvestType;
import edu.mtu.steppables.nipf.Agent;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This agent accepts requests from NIPF agents and harvests their land based upon 
 * various rules. s 
 */
@SuppressWarnings("serial")
public class Harvester implements Steppable {
	/**
	 * This private class is used to wrap harvest requests.
	 */
	private class HarvestRequest {
		public Agent agent;
		public Point[] stand;
		public HarvestType type;
		public int queueOrder;
	}
	
	private List<HarvestRequest> requests = new ArrayList<HarvestRequest>();
	
	/**
	 * 
	 */
	@Override
	public void step(SimState state) {
		// Process the requests
		processHarvestRequests();
		
		// TODO Report results
		
		// Clear the request list
		requests = new ArrayList<HarvestRequest>();		
	}
	
	/**
	 * Process the list of harvest requests and harvest the stands that result
	 * in the most economic returns for the company.
	 */
	private void processHarvestRequests() {
		// TODO Flesh out this method
	}
	
	/**
	 * Allow an agent to request that the forest stand indicated be harvested.
	 * 
	 * @param agent The agent that is requesting the harvest.
	 * @param stand The points that are associated with the stand to be harvested.
	 * @param type The harvest type to be applied.
	 */
	public void requestHarvest(Agent agent, Point[] stand, HarvestType type) {
		// Wrap the data in a request
		HarvestRequest request = new HarvestRequest();
		request.agent = agent;
		request.stand = stand;
		request.type = type;
		
		// Queue it to be processed later
		request.queueOrder = requests.size();
		requests.add(request);
	}
}
