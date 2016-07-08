package edu.mtu.steppables;

import edu.mtu.utilities.LandUseGeomWrapper;
import sim.engine.SimState;

@SuppressWarnings("serial")
public class EcosystemsAgent extends Agent {

	private final static AgentType type = AgentType.ECOSYSTEM;
	
	private double harvestOdds = 0.0;
	
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
	 * Set the odds that the agent will harvest once there is full coverage.
	 */
	public void setHarvestOdds(double value) { harvestOdds = value; }
	
	/**
	 * Apply the rules for this agent to the current simulation state.
	 */
	@Override
	public void step(SimState state) {
		super.step(state);
		
		// If there is full coverage, the agent may harvest with low probability
		if (getLandUse() >= 1.0 && state.random.nextDouble() < harvestOdds) {
			harvest();
		}
	}
}
