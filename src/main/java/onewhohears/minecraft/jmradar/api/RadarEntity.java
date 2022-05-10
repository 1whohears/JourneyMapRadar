package onewhohears.minecraft.jmradar.api;

import java.util.ArrayList;
import java.util.List;

import mcheli.aircraft.MCH_EntityAircraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import onewhohears.minecraft.jmapi.api.ApiWaypointManager;
import onewhohears.minecraft.jmradar.JMRadarMod;
import onewhohears.minecraft.jmradar.config.ConfigManager;

public class RadarEntity {
	
	private String id;
	private double range;
	private EntityLivingBase radar;
	private List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>();
	private boolean active = true;
	private int radarTimer, radarRate;
	private double infoRange = 10;
	
	protected RadarEntity(String id, double range, int radarRate, EntityLivingBase radar) {
		this.id = id;
		this.range = range;
		this.radarRate = radarRate;
		this.radar = radar;
	}
	
	protected RadarEntity(String id, double range, int radarRate, EntityLivingBase radar, EntityPlayerMP player) {
		this.id = id;
		this.range = range;
		this.radarRate = radarRate;
		this.radar = radar;
		players.add(player);
	}
	
	protected RadarEntity(String id, double range, int radarRate, EntityLivingBase radar, EntityPlayerMP player, double infoRange) {
		this.id = id;
		this.range = range;
		this.radarRate = radarRate;
		this.radar = radar;
		this.infoRange = infoRange;
		players.add(player);
	}
	
	public String getId() {
		return id;
	}
	
	public double getRange() {
		return range;
	}
	
	public int getRate() {
		return radarRate;
	}
	
	public void setRadarRate(int rate) {
		radarRate = rate;
	}
	
	public double getInfoRange() {
		return infoRange;
	}
	
	public void setInfoRange(double range) {
		infoRange = range;
	}
	
	public Entity getRadarEntity() {
		return radar;
	}
	
	public void addPlayer(EntityPlayerMP player) {
		if (!players.contains(player)) players.add(player);
	}
	
	public void removePlayer(EntityPlayerMP player) {
		if (players.contains(player)) players.remove(player);
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean a) {
		active = a;
	}
	
	public void sendPlayersMessage(String message) {
		for (int i = 0; i < players.size(); ++i) {
			sendPlayerMessage(i, message);
		}
	}
	
	private void sendPlayerMessage(int index, String message) {
		if (index > players.size() || index < 0) return;
		players.get(index).addChatMessage(new ChatComponentText(message));
	}
	
	@SuppressWarnings("unchecked")
	protected void runMcheliRadar() {
		if (radar.isDead) {
			ApiRadarEntity.instance.removeRadar(id);
			return;
		}
		if (!active) return;
		if (radarTimer > 0) { --radarTimer; return; }
		if (radarTimer <= 0) radarTimer = radarRate;
		List<Entity> entities = radar.worldObj.getEntitiesWithinAABB(
				MCH_EntityAircraft.class, radar.boundingBox.expand(range, range, range));
		List<Entity> pings = new ArrayList<Entity>();
		for(int i = 0; i < entities.size(); ++i) {
			MCH_EntityAircraft ping = (MCH_EntityAircraft)entities.get(i);
			if (ping.isBurning() || ping.isDestroyed()) continue;
			String pingDisplayName = ping.getAcInfo().displayName;
			if (pingDisplayName.equals("Fuel truck")) continue;
			if (!ping.getEntityType().equals("Plane")) continue;
			double distance = radar.getDistanceToEntity(ping);
			double stealth = ConfigManager.getPlaneStealth(pingDisplayName);
			if (distance > (range * stealth)) continue;
			if (ping.riddenByEntity == null) continue;
			if (!(ping.riddenByEntity instanceof EntityPlayer)) continue;
			if (radar.isOnSameTeam((EntityPlayer)ping.riddenByEntity)) continue;
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
	
}