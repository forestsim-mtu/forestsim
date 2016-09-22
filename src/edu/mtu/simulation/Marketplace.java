package edu.mtu.simulation;

import java.util.ArrayList;
import java.util.List;

import edu.mtu.steppables.Biorefinary;
import edu.mtu.steppables.Harvester;

/**
 * This class acts as an aggregation point for agents that need to be accessed 
 * by other agents. For example, NIPF owners need to know how to get in touch
 * with the loggers.
 */
public class Marketplace {

	private static Marketplace instance = new Marketplace();
	
	private List<Biorefinary> biorefinaries = new ArrayList<Biorefinary>();
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
	 * Get the list of registered biorefinaries.
	 */
	public List<Biorefinary> getBiorefinaries() { return biorefinaries; }
	
	/**
	 * Get the list of registered harvesters.
	 */
	public List<Harvester> getHarvesters() { return harvesters; }
	
	/**
	 * Register a biorefinary with the marketplace.
	 */
	public void registerBiorefinary(Biorefinary agent) {
		biorefinaries.add(agent);
	}
	
	/**
	 * Register a harvester with the marketplace.
	 */
	public void registerHarvester(Harvester agent) {
		harvesters.add(agent);
	}
}
