package onewhohears.minecraft.jmradar.api;

import mcheli.weapon.MCH_EntityAAMissile;

public class Missile {
	
	public MCH_EntityAAMissile missile;
	private int prevTick;
	private int tickRepeats;
	
	public Missile(MCH_EntityAAMissile missile) {
		this.missile = missile;
	}
	
	public boolean didTicksRepeat() {
		return tickRepeats > 10;
	}
	
	public void setPrevTick(int tick) {
		if (prevTick == tick) ++tickRepeats;
		else tickRepeats = 0;
		prevTick = tick;
	}
	
}
