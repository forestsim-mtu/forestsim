package edu.mtu.steppables.marketplace;

import java.util.ArrayList;
import java.util.List;

import edu.mtu.utilities.Randomizers;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This class acts as an aggregation point for agents that need to be accessed 
 * by other agents. For example, NIPF owners need to know how to get in touch
 * with the loggers.
 */
public class Marketplace {

	private static Marketplace instance = new Marketplace();
	
	private List<HarvesterBase> harvesters = new ArrayList<HarvesterBase>();
	private List<TransporterBase> transporters = new ArrayList<TransporterBase>();
	private List<ProcessorBase> processors = new ArrayList<ProcessorBase>();
		
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
	 * Get the list of registered harvesters.
	 */
	public List<HarvesterBase> getHarvesters() { return harvesters; }
	
	/**
	 * Get the list of registered biomass processors.
	 */
	public List<ProcessorBase> getProcessors() { return processors; }
	
	/**
	 * Get the list of registered biomass transporters.
	 */
	public List<TransporterBase> getTransporters() { return transporters; } 
	
	/**
	 * Register a logger with the marketplace.
	 */
	public void registerHarvester(HarvesterBase agent) {
		harvesters.add(agent);
	}
	
	/**
	 * Register a biomass processor with the marketplace.
	 */
	public void registerProcessor(ProcessorBase agent) {
		processors.add(agent);
	}
	
	/**
	 * Register a biomass transporter with the marketplace.
	 */
	public void registerTransporter(TransporterBase agent) {
		transporters.add(agent);
	}
	
	/**
	 * Iterate through the marketplace and make sure all agents are scheduled.
	 */
	public void scheduleMarketplace(SimState state) {
		scheduleSteppables(harvesters.toArray(), state);
		scheduleSteppables(transporters.toArray(), state);
		scheduleSteppables(processors.toArray(), state);
	}
	
	/**
	 * Iterate through the items provided and add them to the schedule in a randomized fashion. 
	 */
	private void scheduleSteppables(Object[] items, SimState state) {
		// Exit if there is nothing to do
		if (items.length == 0) {
			return;
		}
		
		// Shuffle the array of objects
		Randomizers.shuffle(items, state.random);
		
		// Add them to the schedule
		for (Object item : items) {
			state.schedule.scheduleRepeating((Steppable)item);
		}
	}
}
