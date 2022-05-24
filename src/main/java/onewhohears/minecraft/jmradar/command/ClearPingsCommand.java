package onewhohears.minecraft.jmradar.command;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import onewhohears.minecraft.jmradar.config.ConfigManager;

@SideOnly(Side.CLIENT)
public class ClearPingsCommand extends CommandBase {

	private static final String cmd = "clearpings";
	
	@Override
	public String getCommandName() {
		return cmd;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "clears da pings";
	}
	
	@Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
    	return true;
    }

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length != 0) { sendError("Unknown Command!"); return; } 
		int delete = 0;
		int l = ConfigManager.radarPrefix.length();
		Waypoint[] waypoints = WaypointStore.instance().getAll()
				.toArray(new Waypoint[WaypointStore.instance().getAll().size()]);
		for (int i = 0; i < waypoints.length; ++i) {
			if (waypoints[i].getName().length() >= l) {
				if (waypoints[i].getName().subSequence(0, l).equals(ConfigManager.radarPrefix)) {
					WaypointStore.instance().remove(waypoints[i]);
					++delete;
				}
			}
		}
		if (delete > 0) sendMessage(delete+" pings were removed!");
		else sendMessage("You have no Radar Pings!");
	}
	
	private void sendMessage(String message) {
		ChatComponentText chat = new ChatComponentText(message);
		ChatStyle style = new ChatStyle();
		style.setColor(EnumChatFormatting.YELLOW);
		chat.setChatStyle(style);
		Minecraft.getMinecraft().thePlayer.addChatMessage(chat);
	}
	
	private void sendError(String message) {
		ChatComponentText chat = new ChatComponentText(message);
		ChatStyle style = new ChatStyle();
		style.setColor(EnumChatFormatting.RED);
		chat.setChatStyle(style);
		Minecraft.getMinecraft().thePlayer.addChatMessage(chat);
	}

}
