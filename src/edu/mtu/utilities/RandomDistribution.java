package edu.mtu.utilities;

import ec.util.MersenneTwisterFast;

/**
 * This class provides a means of drawing random numbers from the given distribution.
 */
public class RandomDistribution {
	
	/**
	 * Return a random number that is in the defined normal distribution. 
	 * 
	 * @param mean The mean of the distribution.
	 * @param sd The standard deviation of the distribution.
	 * @param random The random number generator.
	 * @return The random value in the distribution.
	 */
	public static double NormalDistribution(double mean, double sd, MersenneTwisterFast random) {
		return random.nextGaussian() * sd + mean;
	}
}
