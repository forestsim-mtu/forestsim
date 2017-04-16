package edu.mtu.policy;

/**
 * This interface defines operations needed by ForestSim to manage a policy.
 */
public abstract class PolicyBase {

	private Boolean isIntroduced = false;
	
	/**
	 * Called when the simulation resets to allow the policy to reset itself.
	 */
	public abstract void doOnReset();
	
	/**
	 * Called when the policy is introduced into the simulation.
	 */
	public void introduce() {
		isIntroduced = true;
	}
	
	/**
	 * True if the policy has been introduced, false otherwise.
	 */
	public Boolean isIntroduced() {
		return isIntroduced;
	}
	
	/**
	 * Reset the relevant aspects of the policy.
	 */
	public final void reset() {
		isIntroduced = false;
		doOnReset();
	}
}
