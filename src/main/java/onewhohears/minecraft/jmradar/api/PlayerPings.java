package onewhohears.minecraft.jmradar.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import onewhohears.minecraft.jmapi.api.ApiWaypointManager;

public class PlayerPings {
	
	private String playerName;
	private String playerPrefix;
	private List<RadarPing> pings;
	
	protected PlayerPings(String playerName) {
		this.playerName = playerName;
		playerPrefix = playerName;
		if (playerPrefix.length() > ApiRadarEntity.getPrefixLength()) playerPrefix = playerPrefix.substring(0, ApiRadarEntity.getPrefixLength());
		pings = new ArrayList<RadarPing>();
	}
	
	protected void addPing(String prefix, Entity pingEntity, int maxAge) {
		for (int i = 0; i < pings.size(); ++i) {
			if (pings.get(i).getPrefix().equals(prefix) && pings.get(i).getEntity().equals(pingEntity)) {
				pings.get(i).resetAge();
				sendWaypoint(pings.get(i));
				return;
			}
		}
		int number = getUnusedNumber(prefix);
		RadarPing ping = new RadarPing(prefix, number, pingEntity, maxAge);
		pings.add(ping);
		sendWaypoint(ping);
	}
	
	private void sendWaypoint(RadarPing ping) {
		String senderName = playerName;
		if (ping.getPrefix().equals(playerPrefix)) senderName = "Your Onboard Radar";
		ApiWaypointManager.instance.shareWaypointToPlayer((int)ping.getEntity().posX, (int)ping.getEntity().posY, (int)ping.getEntity().posZ, ping.getEntity().dimension, 
				ApiRadarEntity.defaultPingColor, true, ping.getFullName(), senderName, playerName);
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public String getPlayerPrefix() {
		return playerPrefix;
	}
	
	protected void verifyPingAges() {
		//if (pings.size() > 0) System.out.println(playerName+" PING AGES");
		for (int i = 0; i < pings.size(); ++i) {
			String name = pings.get(i).getFullName();
			//System.out.println(name+" age = "+pings.get(i).getAge());
			if (pings.get(i).isTooOld()) {
				ApiWaypointManager.instance.removePlayerWaypoint(playerName, name, true);
				pings.remove(i--);
				//System.out.println("TOO OLD");
			}
		}
	}
	
	public Entity getEntityByName(String pingName) {
		for (int i = 0; i < pings.size(); ++i) {
			if (pings.get(i).getFullName().equals(pingName)) {
				return pings.get(i).getEntity();
			}
		}
		return null;
	}
	
	public boolean isTrackingEntity(Entity entity) {
		for (int i = 0; i < pings.size(); ++i) {
			if (pings.get(i).getEntity().equals(entity)) return true;
		}
		return false;
	}
	
	public String[] getPingNames() {
		String[] names = new String[pings.size()];
		for (int i = 0; i < pings.size(); ++i) {
			names[i] = pings.get(i).getFullName();
		}
		return names;
	}
	
	public String[] getPingPrefixes() {
		String[] prefixes = new String[pings.size()];
		for (int i = 0; i < pings.size(); ++i) {
			prefixes[i] = pings.get(i).getPrefix();
		}
		return prefixes;
	}
	
	protected int getUnusedNumber(String prefix) {
		int n = 0;
		while (true) {
			++n;
			boolean used = false;
			for (int i = 0; i < pings.size(); ++i) {
				if (pings.get(i).getPrefix().equals(prefix)) {
					if (pings.get(i).getNumber() == n) {
						used = true;
						break;
					}
				}
			}
			if (!used) return n;
		}
	}
	
}
