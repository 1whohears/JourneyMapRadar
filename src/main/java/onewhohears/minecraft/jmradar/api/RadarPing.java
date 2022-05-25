package onewhohears.minecraft.jmradar.api;

import net.minecraft.entity.Entity;
import onewhohears.minecraft.jmradar.config.ConfigManager;

public class RadarPing {
	
	private String prefix;
	private int number;
	private Entity trackEntity;
	private int age;
	private int maxAge;
	
	protected RadarPing(String prefix, int number, Entity pingEntity, int maxAge) {
		if (prefix.length() > ApiRadarEntity.getPrefixLength()) prefix = prefix.substring(0, ApiRadarEntity.getPrefixLength());
		this.prefix = prefix;
		this.number = number;
		this.trackEntity = pingEntity;
		this.maxAge = maxAge;
	}
	
	public Entity getEntity() {
		return trackEntity;
	}
	
	public int getAge() {
		return age;
	}
	
	protected boolean isTooOld() {
		return age++ > maxAge + ConfigManager.mcheliPingAgeBuffer;
	}
	
	public String getFullName() {
		return ConfigManager.radarPrefix+prefix+number;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public int getNumber() {
		return number;
	}
	
	protected void resetAge() {
		this.age = 0;
	}
	
}
