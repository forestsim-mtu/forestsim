package edu.mtu.steppables.marketplace;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import edu.mtu.environment.Forest;
import edu.mtu.simulation.ForestSim;

/**
 * This agent accepts requests from NIPF agents and harvests their land based upon 
 * various rules.
 */
@SuppressWarnings("serial")
public class Harvester extends HarvesterBase {
	private static Harvester instance = new Harvester();
	private List<HarvestRequest> requests = new ArrayList<HarvestRequest>();
	
	private double stemBiomass;
	private double totalBiomass;
	private int capacity;
	private int harvestsRequested;
	private int pracelsHarvested;
	
	/**
	 * Constructor.
	 */
	private Harvester() { }
		
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
	public static Harvester getInstance() {
		return instance;
	}
	
	/**
	 * For ForestSim only, force a new harvester into existence.
	 */
	public static Harvester getNewInstance() {
		instance = new Harvester();
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
		
		Forest forest = Forest.getInstance();		
		while (!requests.isEmpty()) {
			
			// Harvest the biomass from the parcel, update the totals
			HarvestRequest request = requests.remove(0);
			Pair<Double, Double> result = forest.harvest(request.stands);
			stemBiomass += result.getValue0();
			totalBiomass += result.getValue1();
			pracelsHarvested++;
			
			// Inform the agent
			request.agent.doHarvestedOperation();
			
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
	
	/**
	 * Log the indicated harvest request with this producer.
	 * 
	 * @param request The request to be logged.
	 */
	public void requestHarvest(HarvestRequest request) {
		request.queueOrder = requests.size();
		requests.add(request);
	}
}
