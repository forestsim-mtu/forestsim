package edu.mtu.wup.species;

import edu.mtu.environment.Species;

public interface WesternUPSpecies extends Species {
	double getDbhGrowth();
	double getMaximumDbh();
	String getDataFile();
	
	/**
	 * Get the value of one cunit of pulpwood.
	 */
	double getPulpwoodValue();
	
	/**
	 * Get the value of one cunit of sawtimber.
	 */
	double getSawtimberValue();
}
