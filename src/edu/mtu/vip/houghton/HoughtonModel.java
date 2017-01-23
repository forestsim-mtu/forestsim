package edu.mtu.vip.houghton;

import edu.mtu.environment.GrowthModel;
import edu.mtu.simulation.ForestSim;
import edu.mtu.simulation.Scorecard;

@SuppressWarnings("serial")
public class HoughtonModel extends ForestSim {
	// Path to default GIS files used in the simulation
	//private static final String defaultCoverFile = "shapefiles/WUP Land Cover/WUPLandCover.asc";
	//private static final String defaultParcelFile = "file:shapefiles/WUP Parcels/WUPParcels.shp";
	
	private static final String defaultCoverFile 		= "shapefiles/Houghton Land Cover/houghtonlandcover.asc";
	private static final String defaultParcelFile 		= "file:shapefiles/Houghton Parcels/houghton_parcels.shp";
	private static final String defaultOutputDirectory 	= "out"; 
	
	/**
	 * Constructor.
	 * @param seed
	 */
	public HoughtonModel(long seed) {
		super(seed);
	}
	
	/**
	 * Get the agglomeration bonus as mills reduction per 1,000 enrolled.
	 */
	public double getAgglomerationBonus() { 
		return VIP.getInstance().getAgglomerationBonus(); 
	}
	
	/**
	 * Get how old the stand may be before it must be harvested.
	 */
	public int getMustHarvestBy() { 
		return VIP.getInstance().getMustHarvestBy();
	}
	
	/**
	 * Get the number of sq.m. enrolled in the VIP program.
	 */
	public double getVipArea() {
		return VIP.getInstance().getSubscribedArea();
	}
	
	/**
	 * Get the flag that indicates if the VIP is active or not.
	 * @return
	 */
	public Boolean getVipEnabled() { 
		return VIP.getInstance().getIsActive();	
	}
	
	/**
	 * Get the number of agents enrolled in the VIP program.
	 */
	public int getVipMembership() {
		return VIP.getInstance().getSubscriptionRate();
	}
	
	/**
	 * Set the agglomeration bonus as mills reduction per 1,000 enrolled.
	 */
	public void setAgglomerationBonus(double value) { 
		VIP.getInstance().setAgglomerationBonus(value); 
	}
	
	/**
	 * Set how old the stand may be before it must be harvested.
	 */
	public void setMustHarvestBy(int value) {
		VIP.getInstance().setMustHarvestBy(value);
	}
	
	/**
	 * Flag to indicate if the VIP should be enabled or not.
	 */
	public void setVipEnabled(Boolean value) { 
		VIP.getInstance().setIsActive(value);
	}

	@Override
	public GrowthModel getGrowthModel() {
		return new WesternUpEvenAgedWholeStand(getRandom());
	}

	@Override
	public void initialize() {
		VIP.getInstance().reset();
	}

	@Override
	public Scorecard getScoreCard() {
		return new HoughtonVipScorecard(getOutputDirectory());
	}

	@Override
	public String getDefaultCoverFile() {
		return defaultCoverFile;
	}

	@Override
	public String getDefaultOutputDirectory() {
		return defaultOutputDirectory;
	}

	@Override
	public String getDefaultParcelFile() {
		return defaultParcelFile;
	}
}
