package edu.mtu.steppables.management;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import edu.mtu.models.Forest;
import edu.mtu.models.StandThinning;
import edu.mtu.models.StockingCondition;

/**
 * This management plan is simple and limits havesting activity to sawtimber or better.
 */
public class SawtimberHarvest extends ManagementPlan {
	
	private final static double sawtimberDbh = 35.56;
	
	private Point[] harvestPlan;
	private double[] percentagePlan;
	
	/**
	 * Create a harvest plan that is based upon 
	 */
	@Override
	public Point[] createHarvestPlan() {
		// Return the plan if it already exists
		if (harvestPlan != null) {
			return harvestPlan;
		}
		
		double area = 0;
		ArrayList<Point> points = new ArrayList<Point>();
		double pixelArea = Forest.getInstance().getPixelArea();
		for (Point point : agent.getCoverPoints()) {
			// Only harvest fully stocked or better
			int stocking = Forest.getInstance().getStandStocking(point);
			if (stocking < StockingCondition.Full.getValue()) {
				continue;
			}
			
			// Continue if the DBH does not match what has been set
			double dbh = Forest.getInstance().getStandDbh(point);
			if (dbh < sawtimberDbh) {
				continue;
			}
						
			// It is, so update the area and points to use
			area += pixelArea;
			points.add(point);
			if (area >= minimumHarvest) {
				return points.toArray(new Point[0]);
			}
		}
		
		// Nothing to harvest
		return null;
	}

	/**
	 * Check to see if a harvest plan can be generated and return true if so, false otherwise.
	 */
	@Override
	public boolean shouldHarvest() {
		// Clear the old harvest plan
		harvestPlan = null;
		
		// Create a new one
		Point[] plan = createHarvestPlan();
		if (plan != null && plan.length > 0) {
			// Cache the harvest plan and return;
			harvestPlan = plan;
			return true;
		}
		
		return false;
	}

	/**
	 * Thinning is not done for plan.
	 */
	@Override
	public List<StandThinning> createThinningPlan() {
		return null;
	}

	/**
	 * Thinning is not done for the plan.
	 */
	@Override
	public double thinningPrecentage() {
		return 0;
	}

	/**
	 * Thinning is not done for the plan.
	 */
	@Override
	public boolean shouldThin() {
		return false;
	}
}
