package onewhohears.minecraft.jmradar.api;

import net.minecraft.entity.Entity;

public class RadarPing {
	
	public String pingName;
	private Entity pingEntity;
	private int age;
	private int maxAge;
	
	protected RadarPing(String pingName, Entity pingEntity, int maxAge) {
		this.pingName = pingName;
		this.pingEntity = pingEntity;
		this.maxAge = maxAge;
	}
	
	public Entity getEntity() {
		return pingEntity;
	}
	
	protected int getAge() {
		return age;
	}
	
	protected boolean isTooOld() {
		return age++ > maxAge;
	}
	
}
