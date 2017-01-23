package edu.mtu.vip.houghton.species;

import edu.mtu.environment.Species;

public interface WesternUPSpecies extends Species {
	double getDbhGrowth();
	double getMaximumDbh();
	String getDataFile();
}
