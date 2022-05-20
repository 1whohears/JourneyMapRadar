package onewhohears.minecraft.jmradar.api;

import java.util.ArrayList;
import java.util.List;

import mcheli.aircraft.MCH_EntityAircraft;
import mcheli.wrapper.W_WorldFunc;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import onewhohears.minecraft.jmapi.api.ApiWaypointManager;
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
	private int defaultColor = 0xe3b016;
	
	protected RadarEntity(String id, double range, int radarRate, EntityLivingBase radar, TargetType targetType, EntityPlayerMP player, double infoRange) {
		this.id = id;
		this.radar = radar;
		this.infoRange = infoRange;
		this.targetType = targetType;
		addPlayer(player);
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
	 * @return int decimal representation of the hex color radar waypoints use
	 */
	public int getDefaultColor() {
		return defaultColor;
	}
	
	/**
	 * @param color set the color of the radar waypoints (use 0x flag)
	 */
	public void setDefaultColor(int color) {
		defaultColor = color;
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
		if (!radar.worldObj.getChunkProvider().chunkExists(radar.chunkCoordX, radar.chunkCoordZ)) return;
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
		if (prefix.length() > ApiRadarEntity.getPrefixLength()) prefix = prefix.substring(0, ApiRadarEntity.getPrefixLength());
		for (int i = 0; i < pings.size(); ++i) {
			String waypointName = ApiRadarEntity.radarPrefix+prefix+i;
			String waypoint = ApiWaypointManager.instance.createFormattedString(waypointName, 
					(int)pings.get(i).posX, (int)pings.get(i).posY, (int)pings.get(i).posZ, pings.get(i).dimension, defaultColor, true);
			updatePlayersPings(waypoint, pings.get(i), prefix, i);
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
		if (prefix.length() > ApiRadarEntity.getPrefixLength()) prefix = prefix.substring(0, ApiRadarEntity.getPrefixLength());
		for (int i = 0; i < pings.size(); ++i) {
			String waypointName = ApiRadarEntity.radarPrefix+prefix+i;
			String waypoint = ApiWaypointManager.instance.createFormattedString(waypointName, 
					(int)pings.get(i).posX, (int)pings.get(i).posY, (int)pings.get(i).posZ, pings.get(i).dimension, defaultColor, true);
			updatePlayersPings(waypoint, pings.get(i), prefix, i);
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
		if (prefix.length() > ApiRadarEntity.getPrefixLength()) prefix = prefix.substring(0, ApiRadarEntity.getPrefixLength());
		for (int i = 0; i < pings.size(); ++i) {
			String waypointName = ApiRadarEntity.radarPrefix+prefix+i;
			String waypoint = ApiWaypointManager.instance.createFormattedString(waypointName, 
					(int)pings.get(i).posX, (int)pings.get(i).posY, (int)pings.get(i).posZ, pings.get(i).dimension, defaultColor, true);
			updatePlayersPings(waypoint, pings.get(i), prefix, i);
		}
	}
	
	private void updatePlayersPings(String waypoint, Entity ping, String prefix, int number) {
		boolean playerNearBy = false;
		for (int i = 0; i < players.size(); ++i) {
			if (players.get(i).getDistanceToEntity(radar) > infoRange) continue;
			sendPlayerMessage(i, waypoint);
			ApiMcheliBvr.instance.addPing(players.get(i).getDisplayName(), prefix, number, ping, radarRate);
			playerNearBy = true;
		}
		if (playerNearBy && ping.riddenByEntity != null) W_WorldFunc.MOD_playSoundAtEntity(ping.riddenByEntity, "locked", 1.0F, 1.0F);
	}
	
}