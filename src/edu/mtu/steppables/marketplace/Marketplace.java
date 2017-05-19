package edu.mtu.steppables.marketplace;

import java.util.ArrayList;
import java.util.List;

import edu.mtu.simulation.ForestSim;
import edu.mtu.utilities.Randomizers;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This class acts as an aggregation point for agents that need to be accessed 
 * by other agents. For example, NIPF owners need to know how to get in touch
 * with the loggers.
 */
@SuppressWarnings("serial")
public class Marketplace implements Steppable {

	private static Marketplace instance = new Marketplace();
	
	private List<BiomassConsumer> bioenergyPlants = new ArrayList<BiomassConsumer>();
	private List<BiomassConsumer> biorefinaries = new ArrayList<BiomassConsumer>();
	private List<BiomassConsumer> mills = new ArrayList<BiomassConsumer>();
	private List<BiomassConsumer> transporters = new ArrayList<BiomassConsumer>();
	
	private List<BiomassProducer> loggers = new ArrayList<BiomassProducer>();
	
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
	public List<BiomassProducer> getLoggers() { return loggers; }
	
	/**
	 * Get the list of registered mills.
	 */
	public List<BiomassConsumer> getMills() { return mills; }
	
	/**
	 * Get the list of registered biomass transporters.
	 */
	public List<BiomassConsumer> getTransporters() { return transporters; } 
	
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
	 * Register a logger with the marketplace.
	 */
	public void registerLogger(BiomassProducer agent) {
		loggers.add(agent);
	}
	
	/**
	 * Register a mill with the marketplace.
	 */
	public void registerMill(BiomassConsumer agent) {
		mills.add(agent);
	}
	
	/**
	 * Register a biomass transporter with the marketplace.
	 * @param agent
	 */
	public void registerTransporter(BiomassConsumer agent) {
		transporters.add(agent);
	}

	public void step(SimState state) {
		scheduleMarketplace((ForestSim)state);
		state.schedule.scheduleOnce(this);
	}
	
	/**
	 * Iterate through the marketplace and make sure all agents are scheduled.
	 */
	private void scheduleMarketplace(ForestSim state) {
		// Loggers come first
		scheduleSteppables(loggers.toArray(), state);
		
		// Then transporters
		scheduleSteppables(transporters.toArray(), state);
		
		// Finally consumers
		scheduleSteppables(bioenergyPlants.toArray(), state);
		scheduleSteppables(biorefinaries.toArray(), state);
		scheduleSteppables(mills.toArray(), state);
	}
	
	/**
	 * Iterate through the items provided and add them to the schedule in a randomized fashion. 
	 */
	private void scheduleSteppables(Object[] items, ForestSim state) {
		// Exit if there is nothing to do
		if (items.length == 0) {
			return;
		}
		
		// Shuffle the array of objects
		Randomizers.shuffle(items, state.getRandom());
		
		// Add them to the schedule
		for (Object item : items) {
			state.schedule.scheduleOnce((Steppable)item);
		}
	}
}
