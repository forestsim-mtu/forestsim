package edu.mtu.wup.nipf;

import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.wup.model.Economics;
import edu.mtu.wup.model.HarvestProjectionDto;
import edu.mtu.wup.model.Harvesting;
import edu.mtu.wup.vip.VipBase;
import edu.mtu.wup.vip.VipFactory;

@SuppressWarnings("serial")
public class EconomicAgent extends NipfAgent {
			
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
				
		// Get the VIP to do calculations
		VipBase vip = VipFactory.getInstance().getVip();
		
		// Compare the preferred method of harvesting to the programs
		double millage = getMillageRate();
		double prefered = projectProfit(getMinimumDbh(), millage);
		millage -= vip.getMillageRateReduction(this, state);
		double program = projectProfit(vip.getMinimumHarvestingDbh(), millage);
		if (program > prefered) {
			enrollInVip();
		}
	}
	
	private double projectProfit(double dbh, double millage) {
		HarvestProjectionDto dto = Harvesting.estimateTimeToHarvest(getParcel(), dbh);
		double taxes = taxesPaid;
		taxes += Economics.assessTaxes(getParcelArea(), millage) * dto.years;
		return dto.value - taxes;
	}	

	@Override
	protected void doHarvestOperation() {
		// Note our taxes since last harvest
		taxesPaid += Economics.assessTaxes(getParcelArea(), getMillageRate());
		investigateHarvesting();
	}

	@Override
	protected double getMinimumDbh() {
		return Harvesting.SawtimberDbh;
	}

	@Override
	protected double getProfitMargin() {
		return 0.1;
	}
}