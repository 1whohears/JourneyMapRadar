package onewhohears.minecraft.jmradar.api;

import java.util.ArrayList;
import java.util.List;

import mcheli.aircraft.MCH_EntityAircraft;
import mcheli.weapon.MCH_EntityAAMissile;
import mcheli.weapon.MCH_SightType;
import mcheli.weapon.MCH_WeaponAAMissile;
import mcheli.weapon.MCH_WeaponInfo;
import mcheli.weapon.MCH_WeaponParam;
import mcheli.weapon.MCH_WeaponSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.IChunkProvider;

public class ApiMcheliBvr {
	
	public static ApiMcheliBvr instance;
	
	private static int maxPingAge = 50; // TODO make a config value
	private List<Missile> missiles;
	private List<PlayerPings> playerPings;
	
	public ApiMcheliBvr() {
		missiles = new ArrayList<Missile>();
		playerPings = new ArrayList<PlayerPings>();
		instance = this;
	}
	
	public static int getMaxMcheliPingAge() {
		return maxPingAge;
	}
	
	public void verifyPingAges() {
		for (int i = 0; i < playerPings.size(); ++i) {
			playerPings.get(i).verifyPingAges();
		}
	}
	
	public void resetPingsByPrefix(String prefix) {
		for (int i = 0; i < playerPings.size(); ++i) {
			playerPings.get(i).resetByPrefix(prefix);
		}
	}
	
	public void addPing(String playerName, String pingName, Entity pingEntity, int maxAge) {
		for (int i = 0; i < playerPings.size(); ++i) {
			if (playerPings.get(i).getPlayerName().equals(playerName)) {
				playerPings.get(i).addPing(pingName, pingEntity, maxAge);
				return;
			}
		}
		playerPings.add(new PlayerPings(playerName));
	}
	
	public Entity getPingEntity(String playerName, String pingName) {
		for (int i = 0; i < playerPings.size(); ++i) {
			if (playerPings.get(i).getPlayerName().equals(playerName)) {
				return playerPings.get(i).getEntityByName(pingName);
			}
		}
		return null;
	}
	
	public String[] getPlayerPingNames(String playerName) {
		for (int i = 0; i < playerPings.size(); ++i) {
			if (playerPings.get(i).getPlayerName().equals(playerName)) {
				return playerPings.get(i).getPingNames();
			}
		}
		return new String[0];
	}
	
	@SuppressWarnings("unchecked")
	public boolean launchMcheliMissile(EntityPlayer user, MCH_EntityAircraft target) {
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
		if (info.sight != MCH_SightType.LOCK) { // TODO verify is AAMissile
			sendError(user, "You have not selected an air to air missile!");
			return false; 
		}
		if (!ws.canUse()) {
			sendError(user, "Can't use weapon right now!");
			return false; 
		}
		double minRange = 2000;
		if (user.getDistanceToEntity(target) > minRange) {
			sendError(user, "The Min Range for this Missile is "+minRange);
			return false;
		}
		MCH_WeaponParam prm = new MCH_WeaponParam();
		prm.setPosition(ac.posX, ac.posY, ac.posZ);
		prm.entity = ac;
		prm.user = user;
		prm.option1 = target.getEntityId();
		ac.useCurrentWeapon(prm);
		double searchRange = 5;
		List<MCH_EntityAAMissile> ms = ac.worldObj.getEntitiesWithinAABB(
				MCH_EntityAAMissile.class, ac.boundingBox.expand(searchRange, searchRange, searchRange));
		for (int i = 0; i < ms.size(); ++i) {
			MCH_EntityAAMissile m = ms.get(i);
			if (m.ticksExisted > 1) continue;
			if (!m.shootingEntity.equals(user)) continue;
			if (!m.shootingAircraft.equals(ac)) continue;
			if (!m.targetEntity.equals(target)) continue;
			missiles.add(new Missile(m));
			return true;
		}
		sendError(user, "Could not send targeting data to missile!");
		return false;
	}
	
	public void launchTestMissile(EntityPlayer user, Entity target) {
		if (user == null || target == null) return;
		MCH_EntityAAMissile m = new MCH_EntityAAMissile(user.worldObj, 
				user.posX, user.posY, user.posZ, 
				target.posX, target.posY, target.posZ, 
				user.rotationYaw, user.rotationPitch, 2);
		m.setName("sa-2");
		m.shootingEntity = user;
		//m.shootingAircraft = null;
		m.setTargetEntity(target);
		MCH_WeaponInfo info = new MCH_WeaponInfo(m.getName());
		MCH_WeaponAAMissile w = new MCH_WeaponAAMissile(user.worldObj, 
				Vec3.createVectorHelper(user.posX, user.posY, user.posZ), 
				user.rotationYaw, user.rotationPitch,
				m.getName(), info);
		m.setParameterFromWeapon(w, user, user);
		user.worldObj.spawnEntityInWorld(m);
		Vec3 look = user.getLookVec();
		double speed = 2;
		m.setMotion(look.xCoord*speed, look.yCoord*speed, look.zCoord*speed);
		missiles.add(new Missile(m));
	}
	
	public void runBvrMissiles() {
		if (missiles.size() > 0) System.out.println("Missiles Num = "+missiles.size());
		for (int i = 0; i < missiles.size(); ++i) {
			MCH_EntityAAMissile m = missiles.get(i).missile;
			if (!(m.shootingEntity instanceof EntityPlayer)) {
				missiles.remove(i--);
				continue;
			}
			EntityPlayer p = (EntityPlayer)m.shootingEntity;
			if (m.ticksExisted > 600) {
				m.setDead();
				sendError(p, "Missile Ran Out of Fuel");
			}
			if (missiles.get(i).didTicksRepeat()) {
				m.setDead();
				sendError(p, "Missile Glitched for Unknown Reason Sorry!");
			}
			// TODO die if lost lock unless within a range
			if (m.isDead) {
				missiles.remove(i--);
				sendError(p, "Missile Died");
				continue;
			}
			IChunkProvider cp = p.worldObj.getChunkProvider();
			//IChunkProvider cp = m.worldObj.getChunkProvider(); // doesn't work
			//IChunkProvider cp = MinecraftServer.getServer().worldServers[p.dimension].getChunkProvider(); // doesn't work
			/*int xmin = 0, xmax = 0, zmin = 0, zmax = 0; // doesn't work
			double yawcos = Math.cos(m.rotationYaw * Math.PI / 180D);
			double yawsin = -Math.sin(m.rotationYaw * Math.PI / 180D);
			if (yawcos > 0) zmax = (int)Math.ceil(4*yawcos);
			else if (yawcos < 0) zmin = (int)Math.floor(4*yawcos);
			if (yawsin > 0) xmax = (int)Math.ceil(4*yawsin);
			else if (yawsin < 0) xmin = (int)Math.floor(4*yawsin);*/
			int xmin = -3, xmax = 3, zmin = -3, zmax = 3;
			sendImportant(p, "Chunk: ["+m.chunkCoordX+", "+m.chunkCoordZ+"]");
			sendImportant(p, "Chunks Check x["+xmin+","+xmax+"] z["+zmin+","+zmax+"]");
			for (int x = xmin; x < xmax+1; ++x) {
				for (int z = zmin; z < zmax+1; ++z) {
					int cx = m.chunkCoordX+x;
					int cz = m.chunkCoordZ+z;
					if (!cp.chunkExists(cx, cz)) {
						cp.provideChunk(cx, cz);
						sendInfo(p, "New Chunk: ["+cx+", "+cz+"]");
					}
				}
			}
			//double mx = m.motionX, my = m.motionY, mz = m.motionZ;
			//double speed = (double) Math.sqrt(mx * mx + my * my + mz * mz);
			//sendInfo(p, "Speed = "+speed);
			//sendInfo(p, "Ticks = "+m.ticksExisted);
			missiles.get(i).setPrevTick(m.ticksExisted);
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
