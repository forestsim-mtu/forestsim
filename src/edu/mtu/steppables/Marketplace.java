package edu.mtu.steppables;

import java.util.ArrayList;
import java.util.List;

/**
 * This class acts as an aggregation point for agents that need to be accessed 
 * by other agents. For example, NIPF owners need to know how to get in touch
 * with the loggers.
 */
public class Marketplace {

	private static Marketplace instance = new Marketplace();
	
	private List<BiomassConsumer> bioenergyPlants = new ArrayList<BiomassConsumer>();
	private List<BiomassConsumer> biorefinaries = new ArrayList<BiomassConsumer>();
	private List<Harvester> harvesters = new ArrayList<Harvester>();
	
	/**
	 * Constructor
	 */
	private Marketplace() { }
	
	/**
	 * Get a reference to the marketplace object.
	 * @return
	 */
	public static Marketplace getInstance() { return instance; }
	
	/**
	 * Get the list of registered bioenergy plants.
	 */
	public List<BiomassConsumer> getBioenergyPlants() { return bioenergyPlants; }
	
	/**
	 * Get the list of registered biorefinaries.
	 */
	public List<BiomassConsumer> getBiorefinaries() { return biorefinaries; }
	
	/**
	 * Get the list of registered harvesters.
	 */
	public List<Harvester> getHarvesters() { return harvesters; }
	
	/**
	 * Register a bioenergy plant with the marketplace.
	 */
	public void registerBioenergyPlant(BiomassConsumer agent) {
		bioenergyPlants.add(agent);
	}
	
	/**
	 * Register a biorefinary with the marketplace.
	 */
	public void registerBiorefinary(BiomassConsumer agent) {
		biorefinaries.add(agent);
	}
	
	/**
	 * Register a harvester with the marketplace.
	 */
	public void registerHarvester(Harvester agent) {
		harvesters.add(agent);
	}
}
