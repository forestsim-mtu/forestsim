package edu.mtu.steppables;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Precision;

import edu.mtu.models.Forest;
import edu.mtu.steppables.nipf.Agent;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This agent accepts requests from NIPF agents and harvests their land based upon 
 * various rules.
 */
@SuppressWarnings("serial")
public class Harvester implements Steppable {
	/**
	 * This private class is used to wrap harvest requests.
	 */
	private class HarvestRequest {
		public Agent agent;
		public Point[] stand;
		public int queueOrder;
		public BiomassConsumer deliverTo; 
	}
	
	private final static int totalLoggingCapablity = 200;		// TODO Get an actual value, allow it to be configurable
	
	private static Harvester instance = new Harvester();
	
	private List<HarvestRequest> requests = new ArrayList<HarvestRequest>();
	
	private double biomass;
	
	/**
	 * Constructor.
	 */
	private Harvester() { }
	
	/**
	 * 
	 */
	@Override
	public void step(SimState state) {
		// Process the requests
		biomass = processHarvestRequests();
		
		// Clear the request list
		requests = new ArrayList<HarvestRequest>();		
		
		// Restore the harvester to the schedule
		state.schedule.scheduleOnce(this);
	}
	
	/**
	 * Get the harvested biomass, in metric dry tons
	 */
	public double getBiomass() {
		return Precision.round(biomass / 1000, 2);
	}
	
	/**
	 * Get an instance of the harvester agent.
	 */
	public static Harvester getInstance() {
		return instance;
	}
	
	/**
	 * Process the list of harvest requests and harvest the stands that result
	 * in the most economic returns for the company.
	 */
	private double processHarvestRequests() {
		double biomass = 0;
		int count = 0;
		Forest forest = Forest.getInstance();		
		while (!requests.isEmpty()) {
			HarvestRequest request = requests.remove(0);
			biomass += forest.harvest(request.stand);
			count++;
			if (count >= totalLoggingCapablity) {
				return biomass;
			}
		}
		return biomass;
	}
	
	/**
	 * Allow an agent to request that the forest stand indicated be harvested.
	 * 
	 * @param agent The agent that is requesting the harvest.
	 * @param stand The points that are associated with the stand to be harvested.
	 */
	public void requestHarvest(Agent agent, Point[] stand) {
		// Wrap the data in a request
		HarvestRequest request = new HarvestRequest();
		request.agent = agent;
		request.stand = stand;
		
		// Queue it to be processed later
		request.queueOrder = requests.size();
		requests.add(request);
	}
}
