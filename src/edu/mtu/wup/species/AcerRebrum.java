package edu.mtu.wup.species;

import edu.mtu.utilities.Constants;

// Note that Red Maple should be ready for harvest at sawtimber in about 60 years from seeding
// 
// https://www.na.fs.fed.us/pubs/silvics_manual/volume_2/acer/rubrum.htm
// http://www.nrs.fs.fed.us/pubs/rp/rp_nc257.pdf 
// http://dnr.wi.gov/topic/ForestManagement/documents/24315/51.pdf
public class AcerRebrum implements WesternUPSpecies {		
	@Override
	public double getBiomass(double dbh, double height) {
		// Note that we are ignoring the height for now.
		double beta0 = -2.0127, beta1 = 2.4342;
		return Math.exp(beta0 + beta1 * Math.log(dbh));
	}

	// Get the height of the given stand using the height-diameter equation (Kershaw et al. 2008)
	@Override
	public double getHeight(double dbh) {
		double b1 = 29.007, b2 = 0.053, b3 = 1.175;		
		double height = Constants.DbhTakenAt + b1 * Math.pow(1 - Math.pow(Math.E, -b2 * dbh), b3);
		return height;
	}

	@Override
	public String getName() {
		return "Red Maple";
	}

	@Override
	public double getDbhGrowth() {
		return 0.57;
	}

	@Override
	public double getMaximumDbh() {
		return 76.0;
	}

	@Override
	public String getDataFile() {
		return "data/AcerRebrum.csv";
	}
}
