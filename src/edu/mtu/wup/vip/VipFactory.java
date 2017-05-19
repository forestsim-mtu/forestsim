package edu.mtu.wup.vip;

public class VipFactory {
	
	private static VipFactory instance = new VipFactory();
	
	private boolean policy = false;
	private VipBase vip = null;
	
	/**
	 * The list of policy regimes that may be selected.
	 */
	public enum VipRegime {
		NONE,
		DISCOUNT,
		AGGLOMERATION
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
	public VipBase getVip() {
		if (vip == null) {
			throw new IllegalStateException("Either the current regime (i.e., no VIP) or a VIP needs to be selected.");
		}
		return vip;
	}
	
	/**
	 * True if a policy exists, false otherwise.
	 */
	public boolean policyExists() {
		return policy;
	}
	
	/**
	 * Select the VIP for this model.
	 * 
	 * @param program The program 
	 */
	public void selectVip(VipRegime program) {
		switch (program) {
		case NONE: 
			policy = false;
			vip = null;
			break;
		case DISCOUNT:
			policy = true;
			vip = new VipDiscount();
			break;
		case AGGLOMERATION:
			policy = true;
			vip = new VipAgglomeration();
			break;
		}
	}
}
