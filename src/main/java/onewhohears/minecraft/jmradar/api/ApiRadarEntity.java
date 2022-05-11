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
	 * Register an entity as an entity radar that sends the registered players radar waypoints.
	 * Radars will not be saved when the world unloads.
	 * Entities should register themselves when initialized.
	 * This returns null if the inputed id has already been used.
	 * @param id unique String id used to identity this radar entity
	 * @param range the range this radar could detect an object
	 * @param radarRate number of ticks between radar checks. minimum of 10
	 * @param targetType the type of entities the radar tracks
	 * @param radarEntity the entity that functions as a radar
	 * @param player the player the radar sends the pings to
	 * @param infoRange the max range a player can be from the radar to receive pings
	 * @return returns null if that id already exists
	 */
	public RadarEntity addRadar(String id, double range, int radarRate, TargetType targetType, EntityLivingBase radarEntity, EntityPlayerMP player, double infoRange) {
		if (getRadarById(id) != null) return null;
		RadarEntity re = new RadarEntity(id, range, radarRate, radarEntity, targetType, player, infoRange);
		radars.add(re);
		return re;
	}
	
	/**
	 * Register an entity as an entity radar that sends the registered players radar waypoints.
	 * Radars will not be saved when the world unloads.
	 * Entities should register themselves when initialized.
	 * This returns null if the inputed id has already been used.
	 * @param id unique String id used to identity this radar entity
	 * @param range the range this radar could detect an object
	 * @param radarRate number of ticks between radar checks. minimum of 10
	 * @param targetType the type of entities the radar tracks | 0 = players, 1 = mcheli aircraft, 2 = mobs
	 * @param radarEntity the entity that functions as a radar
	 * @param player the player the radar sends the pings to
	 * @param infoRange the max range a player can be from the radar to receive pings
	 * @return returns null if that id already exists
	 */
	public RadarEntity addRadar(String id, double range, int radarRate, int targetType, EntityLivingBase radarEntity, EntityPlayerMP player, double infoRange) {
		if (targetType < 0 || targetType > TargetType.values().length) return null;
		TargetType type = TargetType.values()[targetType];
		return this.addRadar(id, range, radarRate, type, radarEntity, player, infoRange);
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
	public void runEntityRadars() {
		for (int i = 0; i < radars.size(); ++i) {
			radars.get(i).runRadar();
		}
	}
	
}