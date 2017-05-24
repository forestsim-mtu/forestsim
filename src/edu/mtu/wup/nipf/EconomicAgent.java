package edu.mtu.wup.nipf;

import java.awt.Point;
import java.util.ArrayList;
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
		double dbh = getHarvestDbh();
				
		// Determine when the next harvest should be
		if (nextHarvest == -1) {
			projectHarvests();
		}
			
		// If it is time for the next harvest, do so
		if (state.schedule.getSteps() >= nextHarvest) {
			List<Stand> stands = Harvesting.getHarvestableStands(getParcel(), dbh);
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
		int year = 0;
		double value = 0;
		
		// Note the stands for the projection
		List<Stand> projection = new ArrayList<Stand>();
		for (Point point : getParcel()) {
			projection.add(Forest.getInstance().getStand(point));
		}
		
		// Prime things with the current year
		double dbh = getHarvestDbh();
		List<Stand> harvestable = Harvesting.getHarvestableStands(projection, dbh);
		value = Harvesting.getHarvestValue(harvestable);
		
		// Project from T+1 until the window, note that we are updating our projection
		// list of stands every year by only one increment
		for (int ndx = 1; ndx < projectionWindow; ndx++) {
			projection = Harvesting.projectStands(projection, 1);
			harvestable = Harvesting.getHarvestableStands(projection, dbh);
			double bid = Harvesting.getHarvestValue(harvestable);
			double npv = Economics.npv(bid, rate, ndx);
			if (npv > value) {
				value = npv;
				year = ndx;
			}
		}
		
		// Note the harvest year and return
		nextHarvest = state.schedule.getSteps() + year;
	}
	
	/**
	 * Set the NVP discount rate.
	 */
	public void setDiscountRate(double value) {
		rate = value;
	}
}