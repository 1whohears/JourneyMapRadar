package onewhohears.minecraft.jmradar.config;

public class MissileData {
	
	private String name;
	private double range;
	
	protected MissileData(String name, double range) {
		this.name = name;
		setRange(range);
	}
	
	public String getName() {
		return name;
	}
	
	public double getRange() {
		return range;
	}
	
	public void setRange(double range) {
		if (range < ConfigManager.minRange) range = ConfigManager.minRange;
		else if (range > ConfigManager.maxRange) range = ConfigManager.maxRange;
		this.range = range;
	}
	
}
