package edu.mtu.steppables.nipf;

import java.awt.Point;
import java.util.List;

import ec.util.MersenneTwisterFast;
import edu.mtu.management.StandThinning;
import edu.mtu.models.Forest;

@SuppressWarnings("serial")
public class EconomicAgent extends Agent {
	
	private final static AgentType type = AgentType.ECONOMIC;
		
	/**
	 * Constructor.
	 */
	public EconomicAgent(LandUseGeomWrapper landUseWrapper, MersenneTwisterFast random) {
		super(type, landUseWrapper, random);
	}
	
	/**
	 * Return the agent type we are representing.
	 */
	@Override
	public AgentType getAgentType() { return type;	}
	
	@Override
	protected void doVipOperation() {
		// Return if they are already a member
		if (vipEnrollee) {
			return;
		}			
		
		// Look into the program, the flag with be updated if they join
		investigateVipProgram();
	}

	@Override
	protected void doHarvestOperation() {
		// Economic agents will always harvest when it is profitable
		
		
		
		
		// Check to see if we should harvest
		double biomass = 0.0;
		if (plan.shouldHarvest()) {
			Point[] stands = plan.createHarvestPlan();
			biomass = Forest.getInstance().harvest(stands);
		}
		
		// Check to see if we should thin the forest, depending upon the plan, we might harvest and thin
		if (plan.shouldThin()) {
			List<StandThinning> plans = plan.createThinningPlan();
			biomass += Forest.getInstance().thin(plans);
		}		
		
		// TODO Note the harvested biomasss
	}
}