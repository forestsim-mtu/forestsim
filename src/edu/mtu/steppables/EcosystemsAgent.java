package edu.mtu.steppables;

import edu.mtu.utilities.LandUseGeomWrapper;
import sim.engine.SimState;

@SuppressWarnings("serial")
public class EcosystemsAgent extends Agent {

	private final static AgentType type = AgentType.ECOSYSTEM;
		
	/**
	 * Constructor.
	 */
	public EcosystemsAgent(LandUseGeomWrapper landUseWrapper) {
		super(landUseWrapper);
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
		// Return if there is nothing to do
		if (!plan.shouldHarvest()) {
			return;
		}
		return;
	}
}
