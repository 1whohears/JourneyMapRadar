package onewhohears.minecraft.jmradar.events;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import mcheli.aircraft.MCH_AircraftInfo;
import mcheli.aircraft.MCH_EntityAircraft;
import mcheli.wrapper.W_WorldFunc;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import onewhohears.minecraft.jmradar.JMRadarMod;
import onewhohears.minecraft.jmradar.api.ApiMcheliBvr;
import onewhohears.minecraft.jmradar.api.ApiRadarEntity;
import onewhohears.minecraft.jmradar.config.ConfigManager;

public class EventPlayerTick {
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.side != Side.SERVER) return;
		if (event.phase != TickEvent.Phase.START) return;
		if (event.player == null) return;
		if (JMRadarMod.mcHeliRadar) mcHeliRadar(event.player);
	}
	
	@SuppressWarnings("unchecked")
	private void mcHeliRadar(EntityPlayer player) {
		if (player.ridingEntity == null || !player.isRiding()) return;
		if (!(player.ridingEntity instanceof MCH_EntityAircraft)) return;
		MCH_EntityAircraft playerAircraft = (MCH_EntityAircraft)player.ridingEntity;
		if (playerAircraft.isDestroyed()) return;
		MCH_AircraftInfo info = playerAircraft.getAcInfo();
		if (!info.isEnableEntityRadar) return;
		int heliRate = ConfigManager.getAircraftRadarRate(info.displayName);
		if (EventServerTick.ticks % heliRate != 0) return;
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
			if (!((EntityPlayer)ping.riddenByEntity).canEntityBeSeen(player)) continue;
			pings.add(ping);
			W_WorldFunc.MOD_playSoundAtEntity(ping.riddenByEntity, "locked", 1.0F, 1.0F);
		}
		WorldServer world = MinecraftServer.getServer().worldServerForDimension(player.dimension);
		String playerName = player.getDisplayName();
		Scoreboard board = world.getScoreboard();
		ScorePlayerTeam team = board.getPlayersTeam(playerName);
		List<String> playerNames = null;
		if (team != null) playerNames = new ArrayList<String>(team.getMembershipCollection());
		String prefix = playerName;
		if (prefix.length() > ApiRadarEntity.getPrefixLength()) prefix = prefix.substring(0, ApiRadarEntity.getPrefixLength());
		for (int i = 0; i < pings.size(); ++i) {
			ApiMcheliBvr.instance.addPing(playerName, prefix, pings.get(i), heliRate, ConfigManager.defaultPingColor);
			if (playerNames != null) for (int j = 0; j < playerNames.size(); ++j) {
				if (playerNames.get(j).equals(playerName)) continue;
				ApiMcheliBvr.instance.addPing(playerNames.get(j), prefix, pings.get(i), heliRate, ConfigManager.defaultPingColor);
			}
		}
	}
	
	/*private void sendMessage(EntityPlayer player, String message) {
		player.addChatMessage(new ChatComponentText(message));
	}*/
	
}
