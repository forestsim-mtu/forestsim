package edu.mtu.vip.houghton.species;

import edu.mtu.utilities.Constants;

// https://www.na.fs.fed.us/spfo/pubs/silvics_manual/Volume_1/pinus/strobus.htm
// http://dnr.wi.gov/topic/ForestManagement/documents/24315/31.pdf
public class PinusStrobus implements WesternUPSpecies {
	@Override
	public double getBiomass(double dbh, double height) {
		// Note that we are ignoring the height for now.
		double beta0 = -2.5356, beta1 = 2.4349;
		return Math.exp(beta0 + beta1 * Math.log(dbh));
	}

	@Override
	public double getHeight(double dbh) {
		double b1 = 49.071, b2 = 0.016, b3 = 1.0;
		double height = Constants.DbhTakenAt + b1 * Math.pow(1 - Math.pow(Math.E, -b2 * dbh), b3);
		return height;
	}

	@Override
	public String getName() {
		return "Eastern White Pine";
	}

	@Override
	public double getDbhGrowth() {
		return 102.0;
	}

	@Override
	public double getMaximumDbh() {
		return 48.0;
	}

	@Override
	public String getDataFile() {
		return "data/PinusStrobus.csv";
	}
}
