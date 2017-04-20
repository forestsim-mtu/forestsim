package edu.mtu.wup.vip;

public class VipFactory {
	
	private static VipFactory instance = new VipFactory();
	
	private VIP vip;
	
	public enum VipPrograms {
		None,
		TaxIncentive,
		TaxIncentiveWithGlobalBonus,
		TaxIncentiveWithAgglomerationBonus
	}
	
	/**
	 * Constructor.
	 */
	private VipFactory() { }
	
	/**
	 * Get an instance of the factory.
	 */
	public static VipFactory getInstance() { return instance; }

	/**
	 * Get the VIP that is currently in place.
	 */
	public VIP getVip() {
		if (vip == null) {
			throw new IllegalStateException("Either the current regime (i.e., no VIP) or a VIP needs to be selected.");
		}
		return vip;
	}
	
	public void selectVip(VipPrograms program) {
		switch (program) {
		case None: 
			vip = new NoVip();
			break;
		case TaxIncentive:
			throw new IllegalArgumentException(program.toString());
		case TaxIncentiveWithGlobalBonus:
			throw new IllegalArgumentException(program.toString());
		case TaxIncentiveWithAgglomerationBonus:
			vip = new VipAgglomeration();
			break;
		}
	}
}
