package edu.mtu.steppables.management;

import java.awt.Point;

/**
 * This management plan encapsulates the concept of "let nature take it's course."
 */
public class NaturalManagment extends ManagementPlan {
	/**
	 * The basic rule is to never harvest. 
	 */
	@Override
	public boolean shouldHarvest() {
		return false;
	}

	/**
	 * Harvest plans will never be used.
	 */
	@Override
	public Point[] createHarvestPlan() {
		return null;
	}

	/**
	 * Thinning plans will never be used.
	 */
	@Override
	public Point[] createThinningPlan() {
		return null;
	}

	/**
	 * Thinning plans will never be used.
	 */
	@Override
	public double thinningPrecentage() {
		return 0;
	}

	/**
	 * The basic rule is to never thin.
	 */
	@Override
	public boolean shouldThin() {
		return false;
	}
}
