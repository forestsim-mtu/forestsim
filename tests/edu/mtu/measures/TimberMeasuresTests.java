package edu.mtu.measures;

import org.junit.Assert;
import org.junit.Test;

import edu.mtu.simulation.ForestSimException;

public class TimberMeasuresTests {

	private final static double epsilon = 1E-5;
	
	@Test
	public void imperialDbhToCordTest() throws ForestSimException {
		Assert.assertTrue(Math.abs(0    - TimberMeasures.imperialDbhToCord( 4.0)) < epsilon);
		Assert.assertTrue(Math.abs(0.03 - TimberMeasures.imperialDbhToCord( 5.5)) < epsilon);
		Assert.assertTrue(Math.abs(0.03 - TimberMeasures.imperialDbhToCord( 6.0)) < epsilon);
		Assert.assertTrue(Math.abs(0.08 - TimberMeasures.imperialDbhToCord( 7.0)) < epsilon);
		Assert.assertTrue(Math.abs(0.08 - TimberMeasures.imperialDbhToCord( 8.0)) < epsilon);
		Assert.assertTrue(Math.abs(0.15 - TimberMeasures.imperialDbhToCord(10.0)) < epsilon);
		Assert.assertTrue(Math.abs(0.23 - TimberMeasures.imperialDbhToCord(12.0)) < epsilon);
		Assert.assertTrue(Math.abs(0.33 - TimberMeasures.imperialDbhToCord(14.0)) < epsilon);
		Assert.assertTrue(Math.abs(0.45 - TimberMeasures.imperialDbhToCord(16.0)) < epsilon);
		Assert.assertTrue(Math.abs(0.58 - TimberMeasures.imperialDbhToCord(18.0)) < epsilon);
		Assert.assertTrue(Math.abs(0.73 - TimberMeasures.imperialDbhToCord(20.0)) < epsilon);
		Assert.assertTrue(Math.abs(1.0  - TimberMeasures.imperialDbhToCord(22.0)) < epsilon);
	}
	
	@Test
	public void metricDbhToCordTest() throws ForestSimException {
		Assert.assertTrue(Math.abs(0.03 - TimberMeasures.metricDbhToCord(14.0)) < epsilon);
		Assert.assertTrue(Math.abs(1.0  - TimberMeasures.metricDbhToCord(55.0)) < epsilon);
	}

	// Tests that the algorithm loosely approximates the Scribner table
	@Test
	public void scribnerLogRuleTest() {
		Assert.assertTrue(Math.abs(  0 - TimberMeasures.scribnerLogRule( 5, 10)) < epsilon);
		Assert.assertTrue(Math.abs( 30 - TimberMeasures.scribnerLogRule(10, 10)) < epsilon);
		Assert.assertTrue(Math.abs( 90 - TimberMeasures.scribnerLogRule(15, 10)) < epsilon);
		Assert.assertTrue(Math.abs(310 - TimberMeasures.scribnerLogRule(20, 18)) < epsilon);
		Assert.assertTrue(Math.abs(450 - TimberMeasures.scribnerLogRule(24, 18)) < epsilon);
	}
}
