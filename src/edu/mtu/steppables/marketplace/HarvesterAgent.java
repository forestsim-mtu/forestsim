package edu.mtu.steppables.marketplace;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.environment.StandThinning;
import edu.mtu.simulation.ForestSim;
import edu.mtu.steppables.ParcelAgent;

/**
 * This agent accepts requests from NIPF agents and harvests their land based upon 
 * various rules.
 */
@SuppressWarnings("serial")
public class HarvesterAgent extends HarvesterBase {
	private static HarvesterAgent instance = new HarvesterAgent();
	private List<HarvestRequest> requests = new ArrayList<HarvestRequest>();
	
	private double stemBiomass;
	private double totalBiomass;
	private int capacity;
	private int harvestsRequested;
	private int pracelsHarvested;
	
	/**
	 * Constructor.
	 */
	private HarvesterAgent() { }
		
	/**
	 * Set the first time the harvest method is called.
	 */
	public double getCapacity() {
		return capacity;
	}
	
	/**
	 * Get the stem biomass, in kg (dry weight) 
	 * @return
	 */
	public double getStemBiomass() {
		return stemBiomass;
	}
	
	/**
	 * Get the harvested biomass, in kg (dry weight)
	 */
	public double getTotalBiomass() {
		return totalBiomass;
	}
	
	/**
	 * Get the number of harvest requests.
	 */
	public int getHarvestedRequested() {
		return harvestsRequested;
	}
	
	/**
	 * Get the number of parcels harvested.
	 */
	public int getPracelsHarvested() {
		return pracelsHarvested;
	}
	
	/**
	 * Get an instance of the harvester agent.
	 */
	public static HarvesterAgent getInstance() {
		return instance;
	}
	
	/**
	 * For ForestSim only, force a new harvester into existence.
	 */
	public static HarvesterAgent getNewInstance() {
		instance = new HarvesterAgent();
		return instance;
	}
	
	/**
	 * Process the list of harvest requests and harvest the stands that result
	 * in the most economic returns for the company. Note that this method is
	 * provided as an example and can be overridden to provide more flexibility.
	 * 
	 * @param state The current state of the simulation.
	 * @return The total biomass harvested in kilograms dry weight (kg)
	 */
	public void harvest(ForestSim state) {
		// Note the number of requests
		harvestsRequested = requests.size();
		
		// Note how much we can harvest
		capacity = ((ForestSim)state).getHarvestCapacity();
		
		// Reset
		stemBiomass = 0;
		totalBiomass = 0;
		pracelsHarvested = 0;
		
		Pair<Double, Double> result;
		Forest forest = Forest.getInstance();		
		while (!requests.isEmpty()) {
			
			// Are we thinning or harvesting?
			HarvestRequest request = requests.remove(0);
			if (request.isThinning()) {
				result = forest.thin(request.getPlans());
			} else {
				result = forest.harvest(request.getStands());
			}
			
			// Harvest the biomass from the parcel, update the totals
			stemBiomass += result.getValue0();
			totalBiomass += result.getValue1();	
			pracelsHarvested++;
			
			// Inform the agent
			request.getAgent().doHarvestedOperation();
			
			// Return if we are done
			if (pracelsHarvested >= capacity) {
				requests.clear();
				return;
			}
		}
	}
	
	/**
	 * Returns the units that are used for harvesting.
	 */
	public String productionUnits() {
		return "kilograms dry weight (kg)";
	}
		
	@Override
	public void requestHarvest(HarvestRequest request) {
		requests.add(request);
	}
	
	/**
	 * Allow an agent to request that the forest stands indicated be harvested
	 * 
	 * @param agent The agent that is requesting the harvest.
	 * @param stands The stands that are to be harvested.
	 */
	public final void requestHarvest(ParcelAgent agent, List<Stand> stands) {
		Point[] points = new Point[stands.size()];
		for (int ndx = 0; ndx < stands.size(); ndx++) {
			points[ndx] = stands.get(ndx).point; 
		}
		requestHarvest(agent, points, null);
	}	
		
	/**
	 * Allow an agent to request that the forest stand indicated be harvested.
	 * 
	 * @param agent The agent that is requesting the harvest.
	 * @param stands The points that are associated with the stand to be harvested.
	 * @param deliverTo The BiomassConsumer that the harvested biomass should be delivered to.
	 */
	public final void requestHarvest(ParcelAgent agent, Point[] stands, ProcessorBase deliverTo) {
		HarvestRequest request = HarvestRequest.createHarvestRequest(agent, stands, deliverTo);
		requests.add(request);
	}
	
	@Override
	public void requestThinning(HarvestRequest request) {
		requests.add(request);
	}
	
	/**
	 * Allow an agent to request that the forest stands indicated be harvested / thinned.
	 * 
	 * @param agent The agent that is requesting the harvest.
	 * @param stands The stands that are to be harvested / thinned.
	 */
	public final void requestThinning(ParcelAgent agent, List<StandThinning> plans) {
		requestThinning(agent, plans, null);
	}

	/**
	 * Allow an agent to request that the forest stand indicated be harvested / thinned.
	 * 
	 * @param agent The agent that is requesting the harvest.
	 * @param stand The points that are associated with the stand to be harvested / thinned.
	 * @param deliverTo The BiomassConsumer that the harvested biomass should be delivered to.
	 */
	public final void requestThinning(ParcelAgent agent, List<StandThinning> plans, ProcessorBase deliverTo) {
		HarvestRequest request = HarvestRequest.createHarvestRequest(agent, plans, deliverTo);
		requests.add(request);
	}
}
