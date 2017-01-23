package edu.mtu.utilities;

import ec.util.MersenneTwisterFast;

/**
 * This class provides some means of randomizing objects.
 */
public class Randomizers {
	/**
	 * Randomize the array of objects using the Fisher-Yates shuffle
	 * 
	 * @param items The array of items to be shuffled.
	 * @param random The random number generator in use.
	 */
	public static void shuffle(Object[] items, MersenneTwisterFast random) {
		for (int i = items.length - 1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			Object item = items[index];
			items[index] = items[i];
			items[i] = item;
		}
	}
}
