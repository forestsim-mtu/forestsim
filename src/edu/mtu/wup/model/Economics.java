package edu.mtu.wup.model;

import org.apache.commons.math3.util.Precision;

/**
 * This class provides a means of encapsulating some basic economics for the simulation.
 */
public class Economics {

	private final static double assesedValue = 1500.0;				// Assessed value per acre / 4046.86 sq.m.
			
	/**
	 * Assess the taxes on the property.
	 * 
	 * @param area The area of the parcel in acres.
	 * @return The annual taxes due, to two decimals.
	 */
	public static double assessTaxes(double area, double millageRate) {
		double av = area * assesedValue;
		double taxes = (av / 1000) * millageRate;
		return Precision.round(taxes, 2);
	} 
}
