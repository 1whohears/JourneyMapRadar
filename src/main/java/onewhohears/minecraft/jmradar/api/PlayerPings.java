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
				//pings.get(i).resetPrefixNumber(prefix, number);
				return;
			}
		}
		pings.add(new RadarPing(prefix, number, pingEntity, maxAge));
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	protected void resetByPrefix(String prefix) {
		/*for (int i = 0; i < pings.size(); ++i) {
			if (pings.get(i).getPrefix().equals(prefix)) pings.remove(i--);
		}*/
	}
	
	protected void verifyPingAges() {
		System.out.println(playerName+" PING AGES");
		for (int i = 0; i < pings.size(); ++i) {
			String name = pings.get(i).getFullName();
			System.out.println(name+" age = "+pings.get(i).getAge());
			if (pings.get(i).isTooOld()) {
				ApiWaypointManager.instance.removePlayerWaypoint(playerName, name, true); // TODO no delete sometimes?
				pings.remove(i--);
				System.out.println("TOO OLD");
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
	
}
