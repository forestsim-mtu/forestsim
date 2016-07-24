package edu.mtu.steppables;

import java.awt.Point;

import edu.mtu.utilities.LandUseGeomWrapper;
import sim.engine.SimState;

@SuppressWarnings("serial")
public class EconomicAgent extends Agent {
	
	private final static AgentType type = AgentType.ECONOMIC;
	
	/**
	 * Constructor.
	 */
	public EconomicAgent(LandUseGeomWrapper landUseWrapper) {
		super(landUseWrapper);
	}
	
	/**
	 * Return the agent type we are representing.
	 */
	@Override
	public AgentType getAgentType() { return type;	}
	
	/**
	 * Apply the rules for this agent to the current simulation state.
	 */
	@Override
	public void step(SimState state) {
		// Check to see if we should harvest
		double biomass = 0.0;
		if (plan.shouldHarvest()) {
			Point[] stands = plan.createHarvestPlan();
			biomass = harvest(stands, state);
		}
		
		// Check to see if we should thin the forest, depending upon the plan, we might harvest and thin
		if (plan.shouldThin()) {
			Point[] stands = plan.createThinningPlan();
			biomass += thin(stands, plan.thinningPrecentage(), state);
		}
		
		// Note any biomass harvested and return
		// TODO Note the biomass	
		return;
	}
}