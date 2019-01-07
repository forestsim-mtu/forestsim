package edu.mtu.simulation.parameters;

import org.junit.Assert;
import org.junit.Test;

import edu.mtu.simulation.ForestSimException;
import edu.mtu.simulation.parameters.ParameterBase;
import edu.mtu.simulation.parameters.ParseParameters;

public class ParseParametersTests {
	@Test
	public void readTest() throws ForestSimException { 
		// Load the parameters
		ParameterBase parameters = new ParameterBase();
		ParseParameters.read("tests/settings.ini", parameters);
		
		// Check that they are valid
		Assert.assertEquals(768, parameters.getGridHeight());
		Assert.assertEquals(1024, parameters.getGridWidth());
		Assert.assertEquals(false, parameters.getWarningsAsErrors());
		Assert.assertEquals(0.3, parameters.getEconomicAgentPercentage(), 0.001);
		Assert.assertEquals(100, parameters.getFinalTimeStep());
		Assert.assertEquals(25, parameters.getPolicyActiviationStep());
	}
}
