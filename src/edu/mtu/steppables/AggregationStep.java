package edu.mtu.steppables;

import edu.mtu.policy.PolicyBase;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.Scorecard;
import edu.mtu.simulation.parameters.ParameterBase;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This steppable performs any aggregation steps that are needed for the engine
 * as well as insuring the Scorecard is invoked.
 */
@SuppressWarnings("serial")
public class AggregationStep implements Steppable {

	private boolean policyIntroduced = false;
	private Scorecard scorecard = null;
	
	public void step(SimState state) {
		// What's our time-step?
		long step = state.schedule.getSteps() + 1;						// Steps is zero indexed
		ParameterBase parameters = ((ForestSim)state).getBaseParameters();
		
		// Should we end the model?
		if (step >= parameters.getFinalTimeStep()) {
			if (scorecard != null) {
				scorecard.processFinalization((ForestSim)state);
			}
			state.finish();
			return;
		} 
		
		// Introduce the policy if it is time
		if (!policyIntroduced && parameters.getPolicyActiviationStep() == step) {
			PolicyBase policy =	((ForestSim)state).getPolicy();
			if (policy != null) {
				policy.introduce();
			}
			policyIntroduced = true;
		}
			
		// Run the scorecard, if provided
		if (scorecard != null) {
			scorecard.processTimeStep(((ForestSim)state));
		}
	}
	
	/**
	 * Set the score card to be used for aggregation.
	 */
	public void setScorecard(Scorecard scorecard) {
		this.scorecard = scorecard;
	}
}
