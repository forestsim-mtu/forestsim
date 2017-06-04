package edu.mtu.utilities;

/**
 * Utilities for working with numbers.
 */
public class Precision {

	/**
	 * Rounds the given value to the specified number of decimal places.
	 */
	public static double round(double x, int scale) {
		double units = Math.pow(10, scale);
		return Math.round(x * units) / units;
	}
}
