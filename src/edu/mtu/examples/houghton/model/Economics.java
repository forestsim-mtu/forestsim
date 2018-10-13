package edu.mtu.examples.houghton.model;

import edu.mtu.examples.houghton.model.HoughtonParameters;
import edu.mtu.utilities.Precision;

/**
 * This class provides a means of encapsulating some basic economics for the simulation.
 */
public class Economics {
	
	/**
	 * Assess the taxes on the property.
	 * 
	 * @param area The area of the parcel in acres.
	 * @return The annual taxes due, to two decimals.
	 */
	public static double assessTaxes(double area, double millageRate) {
		double av = area * HoughtonParameters.PropertyValue;
		double taxes = (av / 1000) * millageRate;
		return Precision.round(taxes, 2);
	} 
	
	/**
	 * Net present value
	 * 
	 * @param c Current value
	 * @param r Discount rate
	 * @param t Time periods
	 * @return
	 */
	public static double npv(double c, double r, long t) {
		return c / Math.pow(1 + r, t);
	}
}
