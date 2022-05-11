package onewhohears.minecraft.jmradar.api;

import java.util.ArrayList;
import java.util.List;

import mcheli.aircraft.MCH_EntityAircraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import onewhohears.minecraft.jmapi.api.ApiWaypointManager;
import onewhohears.minecraft.jmradar.JMRadarMod;
import onewhohears.minecraft.jmradar.config.ConfigManager;

public class RadarEntity {
	
	private String id;
	private double range;
	private EntityLivingBase radar; // TODO make this a normal entity and find an alternative to EntityLivingBase.canEntityBeSeen()
	private List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>();
	private boolean active = true;
	private int radarTimer, radarRate;
	private double infoRange = 10;
	private TargetType targetType;
	private boolean removeOnDeath = true;
	private int minRadarRate = 10;
	
	protected RadarEntity(String id, double range, int radarRate, EntityLivingBase radar, TargetType targetType) {
		this.id = id;
		this.radar = radar;
		this.targetType = targetType;
		setRange(range);
		setRadarRate(radarRate);
	}
	
	protected RadarEntity(String id, double range, int radarRate, EntityLivingBase radar, TargetType targetType, EntityPlayerMP player) {
		this.id = id;
		this.radar = radar;
		this.targetType = targetType;
		players.add(player);
		setRange(range);
		setRadarRate(radarRate);
	}
	
	protected RadarEntity(String id, double range, int radarRate, EntityLivingBase radar, TargetType targetType, EntityPlayerMP player, double infoRange) {
		this.id = id;
		this.radar = radar;
		this.infoRange = infoRange;
		this.targetType = targetType;
		players.add(player);
		setRange(range);
		setRadarRate(radarRate);
	}
	
	/**
	 * @return this radar's unique id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return the range in blocks this radar can see
	 */
	public double getRange() {
		return range;
	}
	
	/**
	 * @param range max blocks the radar can see
	 */
	public void setRange(double range) {
		if (range < ConfigManager.minRange) range = ConfigManager.minRange;
		else if (range > ConfigManager.maxRange) range = ConfigManager.maxRange;
		this.range = range;
	}
	
	/**
	 * @return number of ticks in between radar detections
	 */
	public int getRadarRate() {
		return radarRate;
	}
	
	/**
	 * @param rate ticks between radar detections
	 */
	public void setRadarRate(int rate) {
		if (rate < minRadarRate) radarRate = minRadarRate;
		radarRate = rate;
	}
	
	/**
	 * @return distance in blocks a player can be to receive info
	 */
	public double getInfoRange() {
		return infoRange;
	}
	
	/**
	 * @param range distance in blocks a player can be to receive info
	 */
	public void setInfoRange(double range) {
		if (range < 0) range = 0;
		else if (range > ConfigManager.maxRange) range = ConfigManager.maxRange;
		infoRange = range;
	}
	
	/**
	 * @return the kind of entities the radar looks for
	 */
	public TargetType getTargetType() {
		return targetType;
	}
	
	/**
	 * @param type the type of entities the radar looks for
	 */
	public void setTargetType(TargetType type) {
		targetType = type;
	}
	
	/**
	 * set the type of entities the radar looks for
	 * @param type 0 = players, 1 = mcheli aircraft, 2 = mobs
	 */
	public void setTargetType(int type) {
		if (type < 0 || type > TargetType.values().length) return;
		targetType = TargetType.values()[type];
	}
	
	/**
	 * @return the entity the radar scans from
	 */
	public EntityLivingBase getRadarEntity() {
		return radar;
	}
	
	/**
	 * @param player new player to receive data from the radar
	 */
	public void addPlayer(EntityPlayerMP player) {
		if (player == null) return;
		if (!players.contains(player)) players.add(player);
	}
	
	/**
	 * @param player don't allow to receive radar data
	 */
	public void removePlayer(EntityPlayerMP player) {
		if (player == null) return;
		if (players.contains(player)) players.remove(player);
	}
	
	/**
	 * @return is the radar on?
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * @param a turn the radar on or off
	 */
	public void setActive(boolean a) {
		active = a;
	}
	
	/**
	 * @return will the radar be unloaded when the entity dies
	 */
	public boolean getRemoveOnDeath() {
		return removeOnDeath;
	}
	
	/**
	 * set to false if the radar entity can respawn
	 * @param remove set if the radar be unloaded when the entity dies
	 */
	public void setRemoveOnDeath(boolean remove) {
		removeOnDeath = remove;
	}
	
	/**
	 * Send all players registered to this radar a message
	 * @param message
	 */
	public void sendPlayersMessage(String message) {
		for (int i = 0; i < players.size(); ++i) {
			sendPlayerMessage(i, message);
		}
	}
	
	private void sendPlayerMessage(int index, String message) {
		if (index > players.size() || index < 0) return;
		if (radar.getDistanceToEntity(players.get(index)) > infoRange) return;
		players.get(index).addChatMessage(new ChatComponentText(message));
	}
	
	protected void runRadar() {
		if (removeOnDeath && radar.isDead) {
			ApiRadarEntity.instance.removeRadar(id);
			return;
		}
		if (!active) return;
		if (radarTimer > 0) { --radarTimer; return; }
		if (radarTimer <= 0) radarTimer = radarRate;
		if (players.size() < 1) return;
		switch (targetType) {
		case PLAYER: runPlayerRadar(); break;
		case MCHELI: runMcheliRadar(); break;
		case MOB:    runMobRadar();    break;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void runPlayerRadar() {
		List<EntityPlayerMP> entities = radar.worldObj.getEntitiesWithinAABB(
				EntityPlayerMP.class, radar.boundingBox.expand(range, range, range));
		List<EntityPlayerMP> pings = new ArrayList<EntityPlayerMP>();
		for(int i = 0; i < entities.size(); ++i) {
			EntityPlayerMP ping = entities.get(i);
			if (ping.isDead) continue;
			double distance = radar.getDistanceToEntity(ping);
			if (distance > range) continue;
			if (players.contains(ping)) continue;
			if (!radar.canEntityBeSeen(ping)) continue;
			pings.add(ping);
		}
		String prefix = id;
		if (prefix.length() > 4) prefix = prefix.substring(0, 5);
		prefix = JMRadarMod.mcHeliPrefix+prefix;
		for (int i = 0; i < pings.size(); ++i) {
			String waypoint = ApiWaypointManager.instance.createFormattedString(prefix+i, 
					(int)pings.get(i).posX, (int)pings.get(i).posY, (int)pings.get(i).posZ, pings.get(i).dimension, 0xe3b016, true);
			sendPlayersMessage(waypoint);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void runMcheliRadar() {
		List<MCH_EntityAircraft> entities = radar.worldObj.getEntitiesWithinAABB(
				MCH_EntityAircraft.class, radar.boundingBox.expand(range, range, range));
		List<MCH_EntityAircraft> pings = new ArrayList<MCH_EntityAircraft>();
		for(int i = 0; i < entities.size(); ++i) {
			MCH_EntityAircraft ping = entities.get(i);
			if (ping.isBurning() || ping.isDestroyed()) continue;
			String pingDisplayName = ping.getAcInfo().displayName;
			if (pingDisplayName.equals("Fuel truck")) continue;
			if (!ping.getEntityType().equals("Plane")) continue;
			double distance = radar.getDistanceToEntity(ping);
			double stealth = ConfigManager.getPlaneStealth(pingDisplayName);
			if (distance > (range * stealth)) continue;
			if (ping.riddenByEntity == null) continue;
			if (!(ping.riddenByEntity instanceof EntityPlayer)) continue;
			if (players.contains(ping.riddenByEntity)) continue;
			if (players.get(0).isOnSameTeam((EntityPlayer)ping.riddenByEntity)) continue;
			if (!radar.canEntityBeSeen(ping)) continue;
			pings.add(ping);
		}
		String prefix = id;
		if (prefix.length() > 4) prefix = prefix.substring(0, 5);
		prefix = JMRadarMod.mcHeliPrefix+prefix;
		for (int i = 0; i < pings.size(); ++i) {
			String waypoint = ApiWaypointManager.instance.createFormattedString(prefix+i, 
					(int)pings.get(i).posX, (int)pings.get(i).posY, (int)pings.get(i).posZ, pings.get(i).dimension, 0xe3b016, true);
			sendPlayersMessage(waypoint);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void runMobRadar() {
		List<EntityMob> entities = radar.worldObj.getEntitiesWithinAABB(
				EntityMob.class, radar.boundingBox.expand(range, range, range));
		List<EntityMob> pings = new ArrayList<EntityMob>();
		for(int i = 0; i < entities.size(); ++i) {
			EntityMob ping = entities.get(i);
			if (ping.isDead) continue;
			double distance = radar.getDistanceToEntity(ping);
			if (distance > range) continue;
			if (!radar.canEntityBeSeen(ping)) continue;
			pings.add(ping);
		}
		String prefix = id;
		if (prefix.length() > 4) prefix = prefix.substring(0, 5);
		prefix = JMRadarMod.mcHeliPrefix+prefix;
		for (int i = 0; i < pings.size(); ++i) {
			String waypoint = ApiWaypointManager.instance.createFormattedString(prefix+i, 
					(int)pings.get(i).posX, (int)pings.get(i).posY, (int)pings.get(i).posZ, pings.get(i).dimension, 0xe3b016, true);
			sendPlayersMessage(waypoint);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void runTestRadar() {
		System.out.println("RADAR TEST: "+id);
		List<EntityLivingBase> entities = radar.worldObj.getEntitiesWithinAABB(
				EntityLivingBase.class, radar.boundingBox.expand(range, range, range));
		List<EntityLivingBase> pings = new ArrayList<EntityLivingBase>();
		for(int i = 0; i < entities.size(); ++i) {
			EntityLivingBase ping = entities.get(i);
			if (ping.equals(radar)) continue;
			if (ping.isDead) continue;
			double distance = radar.getDistanceToEntity(ping);
			if (distance > range) continue;
			if (!radar.canEntityBeSeen(ping)) continue;
			pings.add(ping);
			System.out.println("Adding Ping: "+ping);
		}
		String prefix = id;
		if (prefix.length() > 4) prefix = prefix.substring(0, 5);
		prefix = JMRadarMod.mcHeliPrefix+prefix;
		for (int i = 0; i < pings.size(); ++i) {
			String waypoint = ApiWaypointManager.instance.createFormattedString(prefix+i, 
					(int)pings.get(i).posX, (int)pings.get(i).posY, (int)pings.get(i).posZ, pings.get(i).dimension, 0xe3b016, true);
			sendPlayersMessage(waypoint);
		}
	}
	
}