package onewhohears.minecraft.jmradar.api;

import java.util.ArrayList;
import java.util.List;

import mcheli.aircraft.MCH_EntityAircraft;
import mcheli.weapon.MCH_EntityAAMissile;
import mcheli.weapon.MCH_WeaponInfo;
import mcheli.weapon.MCH_WeaponParam;
import mcheli.weapon.MCH_WeaponSet;
import mcheli.wrapper.W_WorldFunc;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.chunk.IChunkProvider;
import onewhohears.minecraft.jmradar.config.ConfigManager;

public class ApiMcheliBvr {
	
	public static ApiMcheliBvr instance;
	
	private List<Missile> missiles;
	private List<PlayerPings> playerPings;
	
	public ApiMcheliBvr() {
		missiles = new ArrayList<Missile>();
		playerPings = new ArrayList<PlayerPings>();
		instance = this;
	}
	
	public void addPing(String playerName, String prefix, Entity pingEntity, int maxAge, int color) {
		if (prefix.length() > ApiRadarEntity.getPrefixLength()) prefix = prefix.substring(0, ApiRadarEntity.getPrefixLength());
		for (int i = 0; i < playerPings.size(); ++i) {
			if (playerPings.get(i).getPlayerName().equals(playerName)) {
				playerPings.get(i).addPing(prefix, pingEntity, maxAge, color);
				return;
			}
		}
		PlayerPings p = new PlayerPings(playerName);
		p.addPing(prefix, pingEntity, maxAge, color);
		playerPings.add(p);
	}
	
	public Entity getPingEntity(String playerName, String pingName) {
		for (int i = 0; i < playerPings.size(); ++i) {
			if (playerPings.get(i).getPlayerName().equals(playerName)) {
				return playerPings.get(i).getEntityByName(pingName);
			}
		}
		return null;
	}
	
	public boolean isPlayerTrackingEntity(String playerName, Entity entity) {
		for (int i = 0; i < playerPings.size(); ++i) {
			if (playerPings.get(i).getPlayerName().equals(playerName)) {
				return playerPings.get(i).isTrackingEntity(entity);
			}
		}
		return false;
	}
	
	public String[] getPlayerPingNames(String playerName) {
		for (int i = 0; i < playerPings.size(); ++i) {
			if (playerPings.get(i).getPlayerName().equals(playerName)) {
				return playerPings.get(i).getPingNames();
			}
		}
		return new String[0];
	}
	
	public String[] getPlayerPrefixes(String playerName) {
		for (int i = 0; i < playerPings.size(); ++i) {
			if (playerPings.get(i).getPlayerName().equals(playerName)) {
				return playerPings.get(i).getPingPrefixes();
			}
		}
		return new String[0];
	}
	
	/**
	 * If the user is riding an mcheli aircraft and selected an Air to Air missile, the aircraft will fire the missile at the target entity
	 * @param user the player riding the aircraft
	 * @param target the entity the missile should shoot
	 * @return if the launch was sussessful
	 */
	@SuppressWarnings("unchecked")
	public boolean launchMcheliMissile(EntityPlayer user, Entity target) {
		if (!ConfigManager.bvrMode) return false;
		if (user == null || target == null) return false;
		if (user.ridingEntity == null || !(user.ridingEntity instanceof MCH_EntityAircraft)) {
			sendError(user, "You are not Riding an Mcheli Aircraft!");
			return false;
		}
		MCH_EntityAircraft ac = (MCH_EntityAircraft)user.ridingEntity;
		MCH_WeaponSet ws = ac.getCurrentWeapon(user);
		if (ws == null) {
			sendError(user, "You have not selected a weapon!");
			return false;
		}
		MCH_WeaponInfo info = ws.getInfo();
		if (!info.getWeaponTypeName().equals("AA Missile")) {
			sendError(user, "You have not selected an Air to Air Missile!");
			return false; 
		}
		if (!ws.canUse()) {
			sendError(user, "Can't use weapon right now!");
			return false; 
		}
		double minRange = ConfigManager.getBvrMissileMaxRange(info.displayName);
		if (user.getDistanceToEntity(target) > minRange) {
			sendError(user, "The Min Range for this Missile is "+minRange);
			return false;
		}
		if (ws.getRestAllAmmoNum() > 0 && ws.getAmmoNum() < 1) {
			sendError(user, "Reloading!");
			return false;
		}
		if (ws.getRestAllAmmoNum() < 1 && ws.getAmmoNum() < 1) {
			sendError(user, "Out of Ammo!");
			return false;
		}
		MCH_WeaponParam prm = new MCH_WeaponParam();
		prm.setPosition(ac.posX, ac.posY, ac.posZ);
		prm.entity = ac;
		prm.user = user;
		prm.option1 = target.getEntityId();
		ac.useCurrentWeapon(prm);
		double searchRange = 10;
		List<MCH_EntityAAMissile> ms = ac.worldObj.getEntitiesWithinAABB(
				MCH_EntityAAMissile.class, ac.boundingBox.expand(searchRange, searchRange, searchRange));
		for (int i = 0; i < ms.size(); ++i) {
			MCH_EntityAAMissile m = ms.get(i);
			if (m.ticksExisted > 3) continue;
			if (!m.shootingEntity.equals(user)) continue;
			if (!m.shootingAircraft.equals(ac)) continue;
			if (!m.targetEntity.equals(target)) continue;
			missiles.add(new Missile(m));
			sendInfo(user, "Missile Fired!");
			return true;
		}
		sendError(user, "Could not send targeting data to missile!");
		return false;
	}
	
	private static int alertTime = 20;
	private int alertTimer = 0;
	
	/**
	 * already called in EventServerTick
	 */
	public void runBvrMissiles() {
		if (!ConfigManager.bvrMode) return;
		verifyPingAges();
		if (alertTimer > 0) --alertTimer;
		else if (alertTimer <= 0) alertTimer = alertTime;
		for (int i = 0; i < missiles.size(); ++i) {
			MCH_EntityAAMissile m = missiles.get(i).missile;
			if (!(m.shootingEntity instanceof EntityPlayer)) {
				missiles.remove(i--);
				continue;
			}
			EntityPlayer p = (EntityPlayer)m.shootingEntity;
			if (m.ticksExisted > ConfigManager.maxMcheliBvrMissileAge) {
				sendError(p, "Missile Ran Out of Fuel");
				missiles.remove(i--);
				m.setDead();
				continue;
			} 
			if (missiles.get(i).didTicksRepeat()) {
				sendError(p, "Missile Glitched for Unknown Reason Sorry!");
				m.setDead();
				missiles.remove(i--);
				continue;
			}
			Entity target = m.targetEntity;
			if (target == null) {
				sendError(p, "Missile Target Vanished.");
				m.setDead();
				missiles.remove(i--);
				continue;
			}
			double distance = m.getDistanceToEntity(target);
			double pitBullRange = 100d;
			boolean pitBull = distance < pitBullRange;
			// TODO make the missile track invisible stationary entities they fly towards the ping location maybe?
			// removing this for now because laggy servers just kill all the missiles
			// for now missiles will keep going towards the target even if the user looses track
			if (!pitBull && ConfigManager.looseTargetKillMissiles && !isPlayerTrackingEntity(p.getDisplayName(), target)) {
				sendError(p, "Missile lost track of it's target.");
				m.setDead();
				missiles.remove(i--);
				continue;
			}
			if (m.isDead) {
				missiles.remove(i--);
				sendImportant(p, "Missile Exploded or Crashed");
				continue;
			}
			IChunkProvider cp = p.worldObj.getChunkProvider();
			int xmin = -3, xmax = 3, zmin = -3, zmax = 3;
			for (int x = xmin; x < xmax+1; ++x) {
				for (int z = zmin; z < zmax+1; ++z) {
					int cx = m.chunkCoordX+x, cz = m.chunkCoordZ+z;
					if (!cp.chunkExists(cx, cz)) cp.provideChunk(cx, cz);
				}
			}
			if (alertTimer == alertTime && !pitBull) {
				if (target.riddenByEntity != null) W_WorldFunc.MOD_playSoundAtEntity(target.riddenByEntity, "alert", 0.4F, 1.0F);
				else W_WorldFunc.MOD_playSoundAtEntity(target, "alert", 0.4F, 1.0F);
			}
			missiles.get(i).setPrevTick(m.ticksExisted);
		}
	}
	
	private void verifyPingAges() {
		for (int i = 0; i < playerPings.size(); ++i) {
			playerPings.get(i).verifyPingAges();
		}
	}
	
	private void sendInfo(EntityPlayer user, String message) {
		if (user == null) return;
		ChatComponentText chat = new ChatComponentText(message);
		ChatStyle style = new ChatStyle();
		style.setColor(EnumChatFormatting.YELLOW);
		chat.setChatStyle(style);
		user.addChatMessage(chat);
	}
	
	private void sendImportant(EntityPlayer user, String message) {
		if (user == null) return;
		ChatComponentText chat = new ChatComponentText(message);
		ChatStyle style = new ChatStyle();
		style.setColor(EnumChatFormatting.BLUE);
		chat.setChatStyle(style);
		user.addChatMessage(chat);
	}
	
	private void sendError(EntityPlayer user, String message) {
		if (user == null) return;
		ChatComponentText chat = new ChatComponentText(message);
		ChatStyle style = new ChatStyle();
		style.setColor(EnumChatFormatting.RED);
		chat.setChatStyle(style);
		user.addChatMessage(chat);
	}
	
}
