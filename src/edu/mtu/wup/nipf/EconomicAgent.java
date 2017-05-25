package edu.mtu.wup.nipf;

import java.awt.Point;
import java.util.List;

import edu.mtu.environment.Forest;
import edu.mtu.environment.Stand;
import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.steppables.marketplace.AggregateHarvester;
import edu.mtu.wup.model.Economics;
import edu.mtu.wup.model.Harvesting;
import edu.mtu.wup.vip.VipBase;
import edu.mtu.wup.vip.VipFactory;

@SuppressWarnings("serial")
public class EconomicAgent extends NipfAgent {
			
	private final static int projectionWindow = 100;
	
	private double rate = 0.0;	
	private double targetHarvest = -1;
	private long nextHarvest = -1;
		
	/**
	 * Constructor.
	 */
	public EconomicAgent() {
		super(ParcelAgentType.ECONOMIC);
	}
		
	@Override
	protected void doAgentPolicyOperation() {
		// Return if they are already a member
		if (inVip()) {
			return;
		}					
		
		// We want lower taxes, does the VIP give us that?
		VipBase vip = VipFactory.getInstance().getVip();
		if (vip.getMillageRateReduction(this, state) > 0) {
			enrollInVip();
		}
	}
	
	@Override
	protected void doHarvestOperation() {
		// Calculate what our target harvest is
		if (targetHarvest == -1) {
			targetHarvest = getParcelArea() < 40.0 ? getParcelArea() : 40.0;
		}
		
		// Determine when the next harvest should be
		if (nextHarvest == -1) {
			projectHarvests();
		}
			
		// If it is time for the next harvest, do so
		if (state.schedule.getSteps() >= nextHarvest) {
			List<Stand> stands = Harvesting.getHarvestableStands(getParcel(), getHarvestDbh());
			AggregateHarvester.getInstance().requestHarvest(this, stands);
		}
	}
	
	@Override
	public void doHarvestedOperation() {
		super.doHarvestedOperation();
		nextHarvest = -1;		
	}

	@Override
	protected double getMinimumDbh() {
		return Harvesting.SawtimberDbh;
	}
	
	/**
	 * Project the value of future harvests and select the one with the 
	 */
	private void projectHarvests() {
		int down = 0, year = 0;
		double value = 0;
		
		// Note the stands for the projection
		Forest forest = Forest.getInstance();
		Point[] points = getParcel();
		Stand[] projection = new Stand[points.length];
		for (int ndx = 0; ndx < points.length; ndx++) {
			projection[ndx] = forest.getStand(points[ndx]);
		}
		
		// Prime things with the current year
		double dbh = getHarvestDbh();
		value = getBid(projection,  dbh, 0);
		
		// Project from T+1 until the window, note that we are updating our projection
		// list of stands every year by only one increment
		for (int ndx = 1; ndx < projectionWindow; ndx++) {

			// Advance the stands by one year
			for (int ndy = 0; ndy < projection.length; ndy++) {
				projection[ndy] = forest.getGrowthModel().growStand(projection[ndy]);
			}
			
			// Get the bid for the projection
			double npv = getBid(projection, dbh, ndx);
			
			// If the NPV is greater than what we currently have update
			if (npv > value) {
				value = npv;
				year = ndx;
				down = 0;
			}
			
			// If the NPV has gone down for five years, assume we are done
			down += (npv < value) ? 1 : 0;
			if (down >= 5) {
				break;
			}
		}
		
		// Note the harvest year and return
		nextHarvest = state.schedule.getSteps() + year;
	}
	
	/**
	 * Get the bid for the projected growth.
	 */
	private double getBid(Stand[] projection, double dbh, long year) {
		// See what can be harvested
		List<Stand> harvestable = Harvesting.getHarvestableStands(projection, dbh);
		
		// Make sure the area meets the target
		double area = harvestable.size() * Forest.getInstance().getAcresPerPixel();
		if (area < targetHarvest) {
			return 0.0;
		}
		
		// Get the bid and return
		double bid = Harvesting.getHarvestValue(harvestable);
		double npv = Economics.npv(bid, rate, year);
		return npv;
	}
	
	/**
	 * Set the NVP discount rate.
	 */
	public void setDiscountRate(double value) {
		rate = value;
	}
}