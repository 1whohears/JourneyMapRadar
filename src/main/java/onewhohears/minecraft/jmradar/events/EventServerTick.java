package onewhohears.minecraft.jmradar.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import onewhohears.minecraft.jmradar.api.ApiRadarEntity;

public class EventServerTick {
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.ServerTickEvent event) {
		if (event.side != Side.SERVER) return;
		if (event.phase != TickEvent.Phase.END) return;
		if (ApiRadarEntity.instance != null) {
			ApiRadarEntity.instance.runMcheliRadar();
		}
	}
	
}
