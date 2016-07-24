package edu.mtu.steppables.management;

import java.awt.Point;
import java.util.ArrayList;

import edu.mtu.simulation.ForestSim;

/**
 * This management plan is simple and limits havesting activity to sawtimber or better.
 */
public class SawtimberHarvest extends ManagementPlan {
	private Point[] harvestPlan;
	
	/**
	 * 
	 */
	@Override
	public Point[] createHarvestPlan() {
//		double area = 0;
//		ArrayList<Point> points = new ArrayList<Point>();
//		double pixelArea = ((ForestSim)state).getForest().getPixelArea();
//		for (Point point : coverPoints) {
//			// Continue if the DBH does not match what has been set
//			double dbh = ((ForestSim)state).getForest().getStandDbh(point);
//			if (dbh < getMinimumHarvestDbh()) {
//				continue;
//			}
//						
//			// It is, so update the area and points to use
//			area += pixelArea;
//			points.add(point);
//			if (area >= minimumHarvest) {
//				return points.toArray(new Point[0]);
//			}
//		}
		
		// Nothing to harvest
		return null;
	}

	/**
	 * 
	 */
	@Override
	public boolean shouldHarvest() {
		Point[] plan = createHarvestPlan();
		if (plan != null && plan.length > 0) {
			// Cache the harvest plan and return;
			harvestPlan = plan;
			return true;
		}
		return false;
	}
}
