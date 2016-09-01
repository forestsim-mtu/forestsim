package edu.mtu.steppables;

import sim.engine.SimState;

/**
 * This agent accepts requests from NIPF agents and harvests their land based upon 
 * various rules.
 */
@SuppressWarnings("serial")
public class Harvester extends Agent {

	@Override
	public AgentType getAgentType() {
		return AgentType.HARVESTER;
	}

	@Override
	public void step(SimState state) {
		// TODO Auto-generated method stub
	}
}
