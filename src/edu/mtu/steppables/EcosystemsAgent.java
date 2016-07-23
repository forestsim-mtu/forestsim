package edu.mtu.steppables;

import java.awt.Point;

import edu.mtu.utilities.LandUseGeomWrapper;
import sim.engine.SimState;

@SuppressWarnings("serial")
public class EcosystemsAgent extends Agent {

	private final static AgentType type = AgentType.ECOSYSTEM;
		
	/**
	 * Constructor.
	 */
	public EcosystemsAgent(LandUseGeomWrapper landUseWrapper, double cover) {
		super(landUseWrapper);
		setLandUse(cover);
	}

	/**
	 * Return the agent type we are representing.
	 */
	@Override
	public AgentType getAgentType() { return type; }
	
	/**
	 * Apply the rules for this agent to the current simulation state.
	 */
	@Override
	public void step(SimState state) {
		return;
		
		// Get a projected harvest region and profit
//		Point[] stand = createHarvestRegion(minimumHarvest, state);
//		if (stand == null) {
//			return;
//		}
//		double profit = getStandValue(stand, state);
//		
//		// If there is full coverage, the agent may harvest with low probability
//		if (profit >= minimumProfit && state.random.nextDouble() < harvestOdds) {
//			harvest(stand, state);
//		}
	}
}
