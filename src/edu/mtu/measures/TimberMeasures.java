package edu.mtu.measures;

import edu.mtu.simulation.ForestSimException;
import edu.mtu.utilities.Constants;
import edu.mtu.utilities.Precision;

public class TimberMeasures {

	// Two-Inch Diameter class, to cords
	// Note that for the Two-Inch class, values are +/- one inch, e.g., 6-inch = 5.0 to 6.9 in
	// These values are from university extension estimates for the United States
	private final static double[][] dbhToCord = {
			{ 5,  6, 0.03 },
			{ 7,  8, 0.08 },
			{ 9,  10, 0.15 },
			{ 11, 12, 0.23 },
			{ 13, 14, 0.33 },
			{ 15, 16, 0.45 },
			{ 17, 18, 0.58 },
			{ 19, 20, 0.73 },
			{ 21, 22, 1 },
			{ 23,  0, 1 }
	};
	
	/**
	 * Estimate the number of cords based upon the given inputs, estimates are 
	 * conservative based upon Two-Inch classes. 
	 * 
	 * @param dbh The DBH of the tree, in inches.
	 * @return The number of cords in the tree.
	 * @throws ForestSimException Thrown if input is greater than 23.9 inches.
	 */
	public static double imperialDbhToCord(double dbh) throws ForestSimException {
		// Round to the nearest whole number
		dbh = Math.floor(dbh);
		
		// Less than 5 inches, assume zero cords
		if (dbh < 5.0) { return 0; }
		
		// 24 inches or more, throw a bound error
		if (dbh > 23) { throw new ForestSimException("DBH greater than 23.9 inches is not valid for cords conversion."); }
		
		// Return the number of cords
		int ndx = (dbh % 2 == 0) ? 1 : 0;
		for (double[] values : dbhToCord) {
			if (values[ndx] == dbh) { return values[2]; }
		}
		
		// Guard against bad input
		throw new ForestSimException("Invalid DBH (" + dbh + ") encountered.");
	}
		
	/**
	 * Estimate the number of cords based upon the given inputs, estimates are 
	 * conservative based upon Two-Inch classes. 
	 * 
	 * @param dbh The DBH of the tree, in centimeters.
	 * @return The number of cords in the tree.
	 * @throws ForestSimException Thrown if input is greater than 23.9 inches.
	 */
	public static double metricDbhToCord(double dbh) throws ForestSimException {
		return imperialDbhToCord(dbh / Constants.InchToCentimeter);
	}
	
	/**
	 * Estimate the number of board feet from the tree based upon the Scribner Log Rule (Wenger, 1984).
	 * Note that since Scribner is based upon look-up tables, there is some deviation between the formula
	 *  results and the tables
	 * 
	 * @param dib The diameter in bark of the tree, in inches.
	 * @param length The length of the tree, in feet.
	 * @return The number of board feet from the tree, rounded to the nearest 10's of board feet
	 */
	public static double scribnerLogRule(double dib, double length) {
		// Is the DIB too small?
		if (dib < 6) { return 0; }
		
		double bf = (0.79 * Math.pow(dib, 2) - 2 * dib - 4) * (length / 16);
		return Precision.round(bf, 2);
	}
}
