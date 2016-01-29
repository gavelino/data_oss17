package aserg.authorship.distribution;

public class DistributionMetrics {	
	private double focus;
	private int spread;
	
	public DistributionMetrics(int spread, double focus) {
		super();
		this.focus = focus;
		this.spread = spread;
	}
	
	public double getFocus() {
		return focus;
	}
	public int getSpread() {
		return spread;
	}
	
}
