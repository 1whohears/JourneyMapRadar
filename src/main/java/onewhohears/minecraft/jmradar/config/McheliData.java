package onewhohears.minecraft.jmradar.config;

public class McheliData {
	
	private String name;
	private float range;
	private float stealth;
	
	public McheliData(String name, float range) {
		this.name = name;
		setRange(range);
		this.stealth = (float)ConfigManager.defaultMcheliStealth;
	}
	
	public McheliData(float stealth, String name) {
		this.name = name;
		setStealth(stealth);
		this.range = (float)ConfigManager.defaultMcheliRange;
	}
	
	public String getName() {
		return name;
	}
	
	public float getRange() {
		return range;
	}
	
	public void setRange(float range) {
		if (range < ConfigManager.minRange) range = ConfigManager.minRange;
		else if (range > ConfigManager.maxRange) range = ConfigManager.maxRange;
		this.range = range;
	}
	
	public float getStealth() {
		return stealth;
	}
	
	public void setStealth(float stealth) {
		if (stealth < ConfigManager.minStealth) stealth = ConfigManager.minStealth;
		else if (stealth > ConfigManager.maxStealth) stealth = ConfigManager.maxStealth;
		this.stealth = stealth;
	}
	
}
