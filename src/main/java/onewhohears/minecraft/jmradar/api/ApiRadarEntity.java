package onewhohears.minecraft.jmradar.api;

import java.util.ArrayList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

public class ApiRadarEntity {
	
	public static ApiRadarEntity instance;
	
	private ArrayList<RadarEntity> radars;
	
	public ApiRadarEntity() {
		radars = new ArrayList<RadarEntity>();
		instance = this;
	}
	
	/**
	 * register an entity as an entity radar that sends the registered players radar waypoints
	 * all radars are unloaded/not saved when the world isn't loaded
	 * @param id unique String id used to identity this radar entity
	 * @param range the range this radar could detect an object
	 * @param radarRate number of ticks between radar checks
	 * @param radarEntity the entity that functions as a radar
	 * @param player the player the radar sends the pings to
	 * @return returns null if that id already exists
	 */
	public RadarEntity addRadar(String id, double range, int radarRate, EntityLivingBase radarEntity, EntityPlayerMP player) {
		if (getRadarById(id) != null) return null;
		RadarEntity re = new RadarEntity(id, range, radarRate, radarEntity, player);
		radars.add(re);
		return re;
	}
	
	/**
	 * register an entity as an entity radar that sends the registered players radar waypoints
	 * all radars are unloaded/not saved when the world isn't loaded
	 * @param id unique String id used to identity this radar entity
	 * @param range the range this radar could detect an object
	 * @param radarRate number of ticks between radar checks
	 * @param radarEntity the entity that functions as a radar
	 * @param player the player the radar sends the pings to
	 * @param infoRange the max range a player can be from the radar to receive pings
	 * @return returns null if that id already exists
	 */
	public RadarEntity addRadar(String id, double range, int radarRate, EntityLivingBase radarEntity, EntityPlayerMP player, double infoRange) {
		if (getRadarById(id) != null) return null;
		RadarEntity re = new RadarEntity(id, range, radarRate, radarEntity, player, infoRange);
		radars.add(re);
		return re;
	}
	
	/**
	 * remove/unload a radar
	 * @param id
	 * @return false if that id doesn't exist or is already unloaded
	 */
	public boolean removeRadar(String id) {
		for (int i = 0; i < radars.size(); ++i) {
			if (radars.get(i).getId().equals(id)) {
				radars.remove(i);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * get a radar entity object by id to configure a radar entity
	 * @param id
	 * @return
	 */
	public RadarEntity getRadarById(String id) {
		for (int i = 0; i < radars.size(); ++i) {
			if (radars.get(i).getId().equals(id)) return radars.get(i);
		}
		return null;
	}
	
	/**
	 * automatically run in server tick event
	 */
	public void runMcheliRadar() {
		for (int i = 0; i < radars.size(); ++i) {
			radars.get(i).runMcheliRadar();
		}
	}
	
}