package edu.mtu.environment;

import java.awt.Point;

/**
 * This class allows the encapsulation of thinning plans.
 */
public class StandThinning {
	public Point point;
	public double percentage;
	
	/**
	 * Empty constructor.
	 */
	public StandThinning() { } 
	
	/**
	 * Constructor for factory method.
	 */
	public StandThinning(Point point, double percentage) {
		this.point = point;
		this.percentage = percentage;
	}
	
	/**
	 * Factory method for stand thinning.
	 */
	public static StandThinning create(Point point, StandThinningType type) {
		return new StandThinning(point, type.getPercentage());
	}
}
