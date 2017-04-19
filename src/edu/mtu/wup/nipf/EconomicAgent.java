package edu.mtu.wup.nipf;

import edu.mtu.steppables.ParcelAgentType;
import edu.mtu.wup.model.Economics;
import edu.mtu.wup.model.Harvesting;
import edu.mtu.wup.model.VIP;

@SuppressWarnings("serial")
public class EconomicAgent extends NipfAgent {
			
	/**
	 * Constructor.
	 */
	public EconomicAgent() {
		super(ParcelAgentType.ECONOMIC);
		
		minimumDbh = Harvesting.SawtimberDbh;
	}
		
	@Override
	protected void doAgentPolicyOperation() {
		// Return if they are already a member
		if (inVip()) {
			return;
		}			
				
		// Compare the preferred method of harvesting to the programs
		double millage = getMillageRate();
		double prefered = projectProfit(minimumDbh, millage);
		millage -= VIP.getInstance().getMillageRateReduction(this, state);
		double program = projectProfit(VIP.getInstance().getMinimumHarvestingDbh(), millage);
		if (program > prefered) {
			enrollInVip();
		}
	}
	
	@SuppressWarnings("static-access")
	private double projectProfit(double dbh, double millage) {
		Harvesting.HarvestProjectionDto dto = Harvesting.estimateTimeToHarvest(getParcel(), dbh);
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
}