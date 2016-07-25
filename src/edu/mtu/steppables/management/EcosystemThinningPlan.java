package edu.mtu.steppables.management;

import java.awt.Point;
import java.util.ArrayList;

import edu.mtu.models.Forest;
import edu.mtu.models.StockingCondition;

/**
 * This ecosystem services plan allows for basic thining of the tree stands from 
 * over stocked to fully stocked, roughly as per the guidleines of the Healthy
 * Forest Initative.
 * 
 * http://www.sbcounty.gov/calmast/sbc/html/healthy_forest.asp
 * http://www.fs.fed.us/projects/hfi/
 */
public class EcosystemThinningPlan extends ManagementPlan {
	
	private final static double acreInSquareMeters = 4046.86;
	private final static double minimumDbh = 33.02;				// High end of chip-n-saw timber size, in cm.
	private final static int targetTreesPerAcre = 60;			// High end of the healthy forest range
	
	private Point[] thinningPlan;
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
	public Point[] createThinningPlan() {
		// Return the plan if it already exists
		if (thinningPlan != null) {
			return thinningPlan;
		}
		
		ArrayList<Point> points = new ArrayList<Point>();
		double pixelArea = Forest.getInstance().getPixelArea();
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
						
			// It is, so update the area and points to use
			points.add(point);
		}
		
		// Return the points we found
		return (Point[])points.toArray();
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
		Point[] plan = createHarvestPlan();
		if (plan != null && plan.length > 0) {
			// Cache the harvest plan and return;
			thinningPlan = plan;
			return true;
		}
		
		return false;
	}
}
