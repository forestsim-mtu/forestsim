package edu.mtu.management;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import edu.mtu.models.Forest;
import edu.mtu.models.StockingCondition;

/**
 * This ecosystem services plan allows for basic thinning of the tree stands from 
 * over stocked to fully stocked, roughly as per the guidelines of the Healthy
 * Forest Initiative.
 * 
 * http://www.sbcounty.gov/calmast/sbc/html/healthy_forest.asp
 * http://www.fs.fed.us/projects/hfi/
 */
public class HealthyForestInitiative extends ManagementPlan {
	
	private final static double minimumDbh = 33.02;				// High end of chip-n-saw timber size, in cm.
	private final static int targetTreesPerAcre = 60;			// High end of the healthy forest range
	
	private List<StandThinning> thinningPlan;
	private double thinningPercentage;
		
	/**
	 * Harvests do not occur with this plan.
	 */
	@Override
	public Point[] createHarvestPlan() {
		return null;
	}

	/**
	 * 
	 */
	@Override
	public List<StandThinning> createThinningPlan() {
		// Return the plan if it already exists
		if (thinningPlan != null) {
			return thinningPlan;
		}
		
		// Note the target number of trees
		int target = (int)(targetTreesPerAcre / Forest.getInstance().getAcresPerPixel());
		
		List<StandThinning> plans = new ArrayList<StandThinning>();
		for (Point point : agent.getCoverPoints()) {
			// Check to see if the stand is overstocked
			int stocking = Forest.getInstance().getStandStocking(point);
			if (stocking != StockingCondition.Overstocked.getValue()) {
				continue;
			}
						
			// Continue if the DBH does not match what has been set
			double dbh = Forest.getInstance().getStandDbh(point);
			if (dbh < minimumDbh) {
				continue;
			}
			
			// Continue if the stockin does not exceed the guidelines
			int trees = Forest.getInstance().getStandTreeCount(point);
			trees /= Forest.getInstance().getAcresPerPixel();
			if (trees <= target) {
				continue;
			}
			
			// Determine how much to thin by
			StandThinning thinning = new StandThinning();
			thinning.point = point;
			thinning.percentage = target / trees;
						
			// It is, so update the area and points to use
			plans.add(thinning);
		}
		
		// Return the points we found
		return plans;
	}

	@Override
	public double thinningPrecentage() {
		return thinningPercentage;
	}

	/**
	 * Harvests do not occur with this plan.
	 */
	@Override
	public boolean shouldHarvest() {
		return false;
	}

	/**
	 * 
	 */
	@Override
	public boolean shouldThin() {
		// Clear the old harvest plan
		thinningPlan = null;
		
		// Create a new one
		List<StandThinning> plan = createThinningPlan();
		if (plan != null && plan.size() > 0) {
			// Cache the harvest plan and return;
			thinningPlan = plan;
			return true;
		}
		
		return false;
	}
}
