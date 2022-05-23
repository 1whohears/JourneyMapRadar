package onewhohears.minecraft.jmradar.config;

public class McheliData {
	
	private String name;
	private double range;
	private double stealth;
	private int radarRate;
	
	protected McheliData(String name, double range) {
		this.name = name;
		setRange(range);
		this.stealth = ConfigManager.defaultMcheliStealth;
		this.radarRate = ConfigManager.defaultMcheliRadarRate;
	}
	
	protected McheliData(double stealth, String name) {
		this.name = name;
		setStealth(stealth);
		this.range = ConfigManager.defaultMcheliRange;
		this.radarRate = ConfigManager.defaultMcheliRadarRate;
	}
	
	protected McheliData(String name, int radarRate) {
		this.name = name;
		setRadarRate(radarRate);
		this.range = ConfigManager.defaultMcheliRange;
		this.stealth = ConfigManager.defaultMcheliStealth;
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
	
	public double getStealth() {
		return stealth;
	}
	
	public void setStealth(double stealth) {
		if (stealth < ConfigManager.minStealth) stealth = ConfigManager.minStealth;
		else if (stealth > ConfigManager.maxStealth) stealth = ConfigManager.maxStealth;
		this.stealth = stealth;
	}
	
	public int getRadarRate() {
		return radarRate;
	}
	
	public void setRadarRate(int rate) {
		if (rate < ConfigManager.minRate) rate = ConfigManager.minRate;
		else if (rate > ConfigManager.maxRate) rate = ConfigManager.maxRate;
		this.radarRate = rate;
	}
	
	@Override
	public String toString() {
		return "[" + name + " : " + range + " : " + stealth + ":" + radarRate + "]";
	}
	
}
