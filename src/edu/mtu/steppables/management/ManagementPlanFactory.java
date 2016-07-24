package edu.mtu.steppables.management;

import ec.util.MersenneTwisterFast;
import edu.mtu.steppables.Agent;

public class ManagementPlanFactory {
	
	private static ManagementPlanFactory instance = new ManagementPlanFactory();
	
	private double minimumHarvestDbh;
	private double minimumHarvest;
	private MersenneTwisterFast random;
	
	private ManagementPlanFactory() { } 
	
	/**
	 * 
	 * @param planName
	 * @param agent
	 * @return
	 */
	public ManagementPlan createPlan(String planName, Agent agent) {
		ManagementPlan plan = null;
		try {
			plan = (ManagementPlan)Class.forName(planName).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
			System.err.println("Unable to create the management plan: " + ex);
			System.exit(-1);
		}
		
		plan.setAgent(agent);
		plan.setMinimumHarvestArea(minimumHarvest);
		plan.setMinimumHarvestDbh(minimumHarvestDbh);
		plan.setRadom(random);
		return plan;
	}
	
	public static ManagementPlanFactory getInstance() { return instance; }
	
	/**
	 * Set the minimum harvest area
	 */
	public void setMinimumHarvestArea(double value) { minimumHarvest = value; }
	
	/**
	 * Set the minimum harvest DBH.
	 */
	public void setMinimumHarvestDbh(double value) { minimumHarvestDbh = value; }
	
	/**
	 * Set the randomization object to use.
	 * 
	 * @param value The randomization object to use.
	 */
	public void setRadom(MersenneTwisterFast value) { random = value; }
}
