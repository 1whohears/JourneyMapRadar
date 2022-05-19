package onewhohears.minecraft.jmradar.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import onewhohears.minecraft.jmapi.api.ApiWaypointManager;

public class PlayerPings {
	
	private String playerName;
	private List<RadarPing> pings;
	
	protected PlayerPings(String playerName) {
		this.playerName = playerName;
		pings = new ArrayList<RadarPing>();
	}
	
	protected void addPing(String prefix, int number, Entity pingEntity, int maxAge) {
		for (int i = 0; i < pings.size(); ++i) {
			if (pings.get(i).getEntity().equals(pingEntity)) {
				pings.get(i).setPrefixNumber(prefix, number);
				return;
			}
		}
		pings.add(new RadarPing(prefix, number, pingEntity, maxAge));
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	protected void resetByPrefix(String prefix) {
		for (int i = 0; i < pings.size(); ++i) {
			if (pings.get(i).getPrefix().equals(prefix)) pings.remove(i--);
		}
	}
	
	protected void verifyPingAges() {
		for (int i = 0; i < pings.size(); ++i) {
			if (pings.get(i).isTooOld()) {
				ApiWaypointManager.instance.removePlayerWaypoint(playerName, pings.get(i).getFullName());
				pings.remove(i--);
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
	
}
