package onewhohears.minecraft.jmradar.command;

import java.util.List;

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
import onewhohears.minecraft.jmradar.JMRadarMod;

@SideOnly(Side.CLIENT)
public class JMRadarCommand extends CommandBase {
	
	private String cmd = "jmradar";
	
	@Override
	public String getCommandName() {
		return cmd;
	}
	
	@Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
    	return true;
    } 

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "Don't be bad";
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if (args.length == 1) {
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[] {"clearpings"});
		}
		return null;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length == 1) {
			if (args[0].equals("clearpings")) clearPings();
			else sendError("Unknown Command!");
		} else sendError("Unknown Command!");
	}
	
	private void clearPings() {
		Waypoint[] waypoints = WaypointStore.instance().getAll().toArray(new Waypoint[WaypointStore.instance().getAll().size()]);
		int removed = 0;
		for (int i = 0; i < waypoints.length; ++i) {
			if (waypoints[i].getName().substring(0, JMRadarMod.mcHeliPrefix.length()).equals(JMRadarMod.mcHeliPrefix)) {
				WaypointStore.instance().remove(waypoints[i]);
				++removed;
			}
		}
		sendMessage("Removed "+removed+" Pings!");
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
