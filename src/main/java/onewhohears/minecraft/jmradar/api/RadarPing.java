package onewhohears.minecraft.jmradar.api;

import net.minecraft.entity.Entity;

public class RadarPing {
	
	private String prefix;
	private int number;
	private Entity pingEntity;
	private int age;
	private int maxAge;
	
	protected RadarPing(String prefix, int number, Entity pingEntity, int maxAge) {
		if (prefix.length() > ApiRadarEntity.getPrefixLength()) prefix = prefix.substring(0, ApiRadarEntity.getPrefixLength());
		this.prefix = prefix;
		this.number = number;
		this.pingEntity = pingEntity;
		this.maxAge = maxAge;
	}
	
	public Entity getEntity() {
		return pingEntity;
	}
	
	public int getAge() {
		return age;
	}
	
	protected boolean isTooOld() {
		return age++ > maxAge;
	}
	
	public String getFullName() {
		return ApiRadarEntity.radarPrefix+prefix+number;
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
