package onewhohears.minecraft.jmradar.config;

public class McheliData {
	
	private String name;
	private double range;
	private double stealth;
	
	protected McheliData(String name, double range) {
		this.name = name;
		setRange(range);
		this.stealth = ConfigManager.defaultMcheliStealth;
	}
	
	protected McheliData(double stealth, String name) {
		this.name = name;
		setStealth(stealth);
		this.range = ConfigManager.defaultMcheliRange;
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
	
	@Override
	public String toString() {
		return "[" + name + " : " + range + " : " + stealth + "]";
	}
	
}
