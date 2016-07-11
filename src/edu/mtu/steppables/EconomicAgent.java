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
	public EconomicAgent(LandUseGeomWrapper landUseWrapper, double cover) {
		super(landUseWrapper);
		setLandUse(cover);
	}
	
	/**
	 * Apply the rules for this agent to the current simulation state.
	 */
	@Override
	public void step(SimState state) {
		// Get a projected harvest region and profit
		Point[] stand = createHarvestRegion(minimumHarvest, state);
		if (stand == null) {
			return;
		}
		double profit = getStandValue(stand, state);
		
		// Agent will always harvest as soon as there is full coverage
		if (profit > minimumProfit) {
			harvest(stand, state);
		}
	}

	/**
	 * Return the agent type we are representing.
	 */
	@Override
	public AgentType getAgentType() { return type;	}
}