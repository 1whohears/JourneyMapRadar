package onewhohears.minecraft.jmradar.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import onewhohears.minecraft.jmradar.JMRadarMod;

public class PlayerPings {
	
	private String playerName;
	private List<RadarPing> pings;
	
	protected PlayerPings(String playerName) {
		this.playerName = playerName;
		pings = new ArrayList<RadarPing>();
	}
	
	protected void addPing(String pingName, Entity pingEntity, int maxAge) {
		for (int i = 0; i < pings.size(); ++i) {
			if (pings.get(i).getEntity().equals(pingEntity)) {
				pings.get(i).pingName = pingName;
				return;
			}
		}
		pings.add(new RadarPing(pingName, pingEntity, maxAge));
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	protected void resetByPrefix(String prefix) {
		int l = JMRadarMod.mcHeliPrefix.length();
		int l2 = prefix.length();
		for (int i = 0; i < pings.size(); ++i) {
			String name = pings.get(i).pingName;
			String p = name.substring(l, l+l2);
			if (p.equals(prefix)) pings.remove(i--);
		}
	}
	
	protected void verifyPingAges() {
		for (int i = 0; i < pings.size(); ++i) {
			if (pings.get(i).isTooOld()) pings.remove(i--);
		}
	}
	
	public Entity getEntityByName(String pingName) {
		for (int i = 0; i < pings.size(); ++i) {
			if (pings.get(i).pingName.equals(pingName)) {
				return pings.get(i).getEntity();
			}
		}
		return null;
	}
	
	public String[] getPingNames() {
		String[] names = new String[pings.size()];
		for (int i = 0; i < pings.size(); ++i) {
			names[i] = pings.get(i).pingName;
		}
		return names;
	}
	
}
