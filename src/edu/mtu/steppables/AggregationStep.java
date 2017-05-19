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

	private Scorecard scorecard = null;
	
	public void step(SimState state) {
		// Introduce the policy if it is time
		long step = state.schedule.getSteps();
		ParameterBase parameters = ((ForestSim)state).getParameters();
		
		if (parameters.policyActiviationStep() <= (step + 1)) {
			PolicyBase policy =	((ForestSim)state).getPolicy();
			if (policy != null) {
				policy.introduce();
			}
		}
		
		// Run the scorecard, if provided
		if (scorecard != null) {
			scorecard.processTimeStep(((ForestSim)state));
		}
		
		// Should we end the model?
		if ((step + 1) > parameters.getFinalTimeStep()) {
			state.finish();
			return;
		} 
		
		// Put us back in the queue
		state.schedule.scheduleOnce(this);		
	}
	
	/**
	 * Set the score card to be used for aggregation.
	 */
	public void setScorecard(Scorecard scorecard) {
		this.scorecard = scorecard;
	}
}
