package onewhohears.minecraft.jmradar.events;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import mcheli.aircraft.MCH_AircraftInfo;
import mcheli.aircraft.MCH_EntityAircraft;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import onewhohears.minecraft.jmapi.api.ApiWaypointManager;
import onewhohears.minecraft.jmradar.JMRadarMod;
import onewhohears.minecraft.jmradar.config.ConfigManager;

public class EventPlayerTick {
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.side != Side.SERVER) return;
		if (event.phase != TickEvent.Phase.END) return;
		if (event.player == null) return;
		if (JMRadarMod.mcHeliRadar) mcHeliRadar(event.player);
	}
	
	private int heliTimer = 0, heliRate = 40;
	// TODO a system that removes old pings
	
	@SuppressWarnings("unchecked")
	private void mcHeliRadar(EntityPlayer player) {
		if (heliTimer < heliRate) { ++heliTimer; return; }
		if (heliTimer >= heliRate) heliTimer = 0;
		if (player.ridingEntity == null || !player.isRiding()) return;
		if (!(player.ridingEntity instanceof MCH_EntityAircraft)) return;
		MCH_EntityAircraft playerAircraft = (MCH_EntityAircraft)player.ridingEntity;
		MCH_AircraftInfo info = playerAircraft.getAcInfo();
		if (!info.isEnableEntityRadar) return;
		double range = ConfigManager.getPlaneRange(info.displayName);
		List<MCH_EntityAircraft> entities = player.getEntityWorld().getEntitiesWithinAABB(
				MCH_EntityAircraft.class, player.boundingBox.expand(range, range, range));
		List<MCH_EntityAircraft> pings = new ArrayList<MCH_EntityAircraft>();
		for(int i = 0; i < entities.size(); ++i) {
			MCH_EntityAircraft ping = entities.get(i);
			if (ping == playerAircraft) continue;
			if (ping.isBurning() || ping.isDestroyed()) continue;
			String pingDisplayName = ping.getAcInfo().displayName;
			if (pingDisplayName.equals("Fuel truck")) continue;
			if (!ping.getEntityType().equals("Plane")) continue;
			double distance = player.getDistanceToEntity(ping);
			double stealth = ConfigManager.getPlaneStealth(pingDisplayName);
			if (distance > (range * stealth)) continue;
			if (ping.riddenByEntity == null) continue;
			if (!(ping.riddenByEntity instanceof EntityPlayer)) continue;
			if (player.isOnSameTeam((EntityPlayer)ping.riddenByEntity)) continue;
			if (!player.canEntityBeSeen(ping)) continue;
			pings.add(ping);
		}
		String playerName = player.getDisplayName();
		Scoreboard board = Minecraft.getMinecraft().theWorld.getScoreboard();
		ScorePlayerTeam team = board.getPlayersTeam(playerName);
		List<String> playerNames = null;
		if (team != null) playerNames = new ArrayList<String>(team.getMembershipCollection());
		String playerKey = playerName;
		if (playerKey.length() > 4) playerKey = playerKey.substring(0, 5);
		playerKey = JMRadarMod.mcHeliPrefix+playerKey;
		for (int i = 0; i < pings.size(); ++i) {
			String waypoint = ApiWaypointManager.instance.createFormattedString(playerKey+i, 
					(int)pings.get(i).posX, (int)pings.get(i).posY, (int)pings.get(i).posZ, pings.get(i).dimension, 0xe3b016, true);
			sendMessage(player, waypoint);
			if (playerNames != null) for (int j = 0; j < playerNames.size(); ++j) {
				if (playerNames.get(j).equals(playerName)) continue;
				String name = playerKey+i;
				ApiWaypointManager.instance.shareWaypointToPlayer((int)pings.get(i).posX, (int)pings.get(i).posY, (int)pings.get(i).posZ, pings.get(i).dimension, 
						0xe3b016, true, name, playerName, playerNames.get(j));
			}
		}
	}
	
	private void sendMessage(EntityPlayer player, String message) {
		player.addChatMessage(new ChatComponentText(message));
	}
	
}
