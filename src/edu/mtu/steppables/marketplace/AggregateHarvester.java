package edu.mtu.steppables.marketplace;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Precision;

import edu.mtu.environment.Forest;
import edu.mtu.simulation.ForestSim;
import edu.mtu.steppables.ParcelAgent;
import edu.mtu.wup.vip.VipBase;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This agent accepts requests from NIPF agents and harvests their land based upon 
 * various rules.
 */
@SuppressWarnings("serial")
public class AggregateHarvester implements Steppable {
	private static AggregateHarvester instance = new AggregateHarvester();
	private List<HarvestRequest> requests = new ArrayList<HarvestRequest>();
	
	public final static double MinimumHarvestArea = VipBase.baseAcerage;		// Minimum area that can be harvested.
	
	private double biomass;
	private int demand;
	private int harvested;
	
	/**
	 * Constructor.
	 */
	private AggregateHarvester() { }
	
	/**
	 * 
	 */
	@Override
	public void step(SimState state) {
		// Process the requests
		int capacity = ((ForestSim)state).getHarvestCapacity();
		biomass = processHarvestRequests(capacity);
		
		// Clear the request list
		requests = new ArrayList<HarvestRequest>();		
		
		// Restore the harvester to the schedule
		state.schedule.scheduleOnce(this);
	}
	
	/**
	 * Get the harvested biomass, in green tons (GT).
	 */
	public double getBiomass() {
		return Precision.round(biomass, 2);
	}
	
	public int getDemand() {
		return demand;
	}
	
	public int getHarvested() {
		return harvested;
	}
	
	/**
	 * Get an instance of the harvester agent.
	 */
	public static AggregateHarvester getInstance() {
		return instance;
	}
	
	/**
	 * Process the list of harvest requests and harvest the stands that result
	 * in the most economic returns for the company. Note that this method is
	 * provided as an example and can be overridden to provide more flexibility.
	 * 
	 * @param capacity The total capacity for harvests.
	 * @return The total biomass harvested in green tons (GT)
	 */
	protected double processHarvestRequests(int capacity) {
		double totalBiomass = 0;
		
		demand = requests.size();
		harvested = 0;
		
		Forest forest = Forest.getInstance();		
		while (!requests.isEmpty()) {
			HarvestRequest request = requests.remove(0);
			request.agent.doHarvestedOperation();
			
			// Remove the biomass and deliver it if need be
			double biomass = forest.harvest(request.stand);
			if (request.deliverTo != null) 
			{
				request.deliverTo.receive(biomass);
			}
			
			// Update the aggregation
			totalBiomass += biomass;
			harvested++;
			if (harvested >= capacity) {
				return totalBiomass;
			}
		}
		return totalBiomass;
	}
	
	/**
	 * Allow an agent to request that the forest stand indicated be harvested.
	 * 
	 * @param agent The agent that is requesting the harvest.
	 * @param stand The points that are associated with the stand to be harvested.
	 */
	public void requestHarvest(ParcelAgent agent, Point[] stand) {
		requestHarvest(agent, stand, null);
	}
	
	/**
	 * Allow an agent to request that the forest stand indicated be harvested.
	 * 
	 * @param agent The agent that is requesting the harvest.
	 * @param stand The points that are associated with the stand to be harvested.
	 * @param deliverTo The BiomassConsumer that the harvested biomass should be delivered to.
	 */
	public void requestHarvest(ParcelAgent agent, Point[] stand, BiomassConsumer deliverTo) {
		// Wrap the data in a request
		HarvestRequest request = new HarvestRequest();
		request.agent = agent;
		request.stand = stand;
		request.deliverTo = null;
		
		// Queue it to be processed later
		request.queueOrder = requests.size();
		requests.add(request);
	}
}
