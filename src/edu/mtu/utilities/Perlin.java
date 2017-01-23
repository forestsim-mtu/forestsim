package edu.mtu.utilities;

import ec.util.MersenneTwisterFast;
import sim.field.grid.DoubleGrid2D;

/**
 * This class is used to generate Perlin noise (http://dl.acm.org/citation.cfm?doid=325165.325247) which 
 * should allow for more natural random landscapes. 
 */
public class Perlin {
	// This value is set by the developer, 0.5 is recommended as a good starting point
	private final static float Persistance = 0.5f;
	
	/**
	 * Generate a random grid using Perlin noise of the given dimensions.
	 * 
	 * @param height The height of the grid.
	 * @param width The width of the grid.
	 * @param octaves The number of octaves to use for the smoothing.
	 * @param random The random number generator to use for the generation.
	 * @return The generated gird with Perlin noise applied.
	 */
	public static DoubleGrid2D generate(int height, int width, int octaves, MersenneTwisterFast random) {
		// Generate the random noise
		DoubleGrid2D noise = generateWhiteNoise(height, width, random);
		
		// Smooth the random noise
		DoubleGrid2D[] smoothed = new DoubleGrid2D[octaves];
		for (int ndx = 0; ndx < octaves; ndx++) {
			smoothed[ndx] = smoothNoise(noise, ndx);
		}
		
		return generatePerlinNoise(noise, smoothed, octaves);
	}
	
	/**
	 * Generate the Perlin noise based upon the base noise grid, smoothed grids, and the number of octaves.
	 * 
	 * @param noise A grid containing white noise.
	 * @param smoothed Grids based upon the white noise that have been smoothed.
	 * @param octaves The number of octaves of smoothing.
	 * @return The final grid with Perlin noise.
	 */
	private static DoubleGrid2D generatePerlinNoise(DoubleGrid2D noise, DoubleGrid2D[] smoothed, int octaves) {
		DoubleGrid2D grid = new DoubleGrid2D(noise.getWidth(), noise.getHeight());

		float amplitude = 1.0f;
		float totalAmplitude = 0.0f;

		for (int octave = octaves - 1; octave >= 0; octave--) {
			amplitude *= Persistance;
			totalAmplitude += amplitude;

			for (int ndx = 0; ndx < grid.getWidth(); ndx++) {
				for (int ndy = 0; ndy < grid.getHeight(); ndy++) {
					grid.set(ndx, ndy, grid.get(ndx, ndy) + smoothed[octave].get(ndx, ndy) * amplitude); 
				}
			}
		}

		for (int ndx = 0; ndx < grid.getWidth(); ndx++) {
			for (int ndy = 0; ndy < grid.getHeight(); ndy++) {
				grid.set(ndx, ndy, grid.get(ndx, ndy) / totalAmplitude);
			}
		}

		return grid;
	}
		
	/**
	 * Generate a grid of random noise with values between zero and one.
	 * 
	 * @param height The height of the grid.
	 * @param width The width of the grid.
	 * @param random The random number generator to use for the generation.
	 * @return The grid of random white noise.
	 */
	public static DoubleGrid2D generateWhiteNoise(int height, int width, MersenneTwisterFast random) {
		// Create the grid and seed it with random values
		DoubleGrid2D grid = new DoubleGrid2D(width, height);
		for (int ndx = 0; ndx < width; ndx++) {
			for (int ndy = 0; ndy < height; ndy++) {
				grid.set(ndx, ndy, random.nextDouble() % 1);
			}
		}
		return grid;
	}
	
	private static double interpolate(double x0, double x1, double alpha) {
		return x0 * (1 - alpha) + alpha * x1;
	}
	
	/**
	 * Scale a grid containing Perlin noise to be between the min and max values provided.
	 * 
	 * @param grid The grid of values to be scaled.
	 * @param min The minimum value to assign.
	 * @param max The maximum value to assign.
	 * @return The scaled grid of values.
	 */
	public static DoubleGrid2D scaleGrid(DoubleGrid2D grid, double min, double max) {
		for (int ndx = 0; ndx < grid.getWidth(); ndx++) {
			for (int ndy = 0; ndy < grid.getHeight(); ndy++) {   
				double value = min + (max - min) * grid.get(ndx, ndy);
				grid.set(ndx, ndy, value);
			}
		}
		return grid;
	}
	
	/**
	 * Generate smooth noise from the grid provided.
	 * 
	 * @param base The grid to generate smooth noise form.
	 * @param octave The current octave of smoothing.
	 * @return The smoothed grid.
	 */
	private static DoubleGrid2D smoothNoise(DoubleGrid2D base, int octave) {
		DoubleGrid2D grid = new DoubleGrid2D(base.getWidth(), base.getHeight());
		
		// Calculate the sample period and frequency
		int samplePeriod = 1 << octave;
		float sampleFrequency = 1.0f / samplePeriod;
				
		for (int ndx = 0; ndx < base.getWidth(); ndx++) {
			int sample_i0 = (ndx / samplePeriod) * samplePeriod;
			// Ensure that values wrap around
			int sample_i1 = (sample_i0 + samplePeriod) % base.getWidth();
			double horizontal_blend = (ndx - sample_i0) * sampleFrequency;

			for (int ndy = 0; ndy < base.getHeight(); ndy++) {
				int sample_j0 = (ndy / samplePeriod) * samplePeriod;
				int sample_j1 = (sample_j0 + samplePeriod) % base.getHeight();
				double vertical_blend = (ndy - sample_j0) * sampleFrequency;
				double top = interpolate(base.get(sample_i0, sample_j0), base.get(sample_i1, sample_j0), horizontal_blend);
				double bottom = interpolate(base.get(sample_i0, sample_j1), base.get(sample_i1, sample_j1), horizontal_blend);
				grid.set(ndx, ndy, interpolate(top, bottom, vertical_blend));
			}
		}
		return grid;
	}
}
