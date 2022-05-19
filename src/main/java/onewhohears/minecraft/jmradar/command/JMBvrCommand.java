package onewhohears.minecraft.jmradar.command;

import java.util.List;

import mcheli.aircraft.MCH_EntityAircraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import onewhohears.minecraft.jmradar.api.ApiMcheliBvr;

public class JMBvrCommand extends CommandBase {
	
	private String cmd = "bvr";
	
	private EntityPlayer user;
	
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
		return "/"+cmd+" lock/shoot";
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if (args.length == 1) {
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[] {"testshoot", "shoot"});
		} else if (args.length == 2) {
			if (args[0].equals("shoot")) {
				return CommandBase.getListOfStringsMatchingLastWord(args, getPingNames(sender.getCommandSenderName()));
			} else if (args[0].equals("testshoot")) {
				return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
			}
		}
		return null;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		user = sender.getEntityWorld().getPlayerEntityByName(sender.getCommandSenderName());
		if (args.length == 2) {
			if (args[0].equals("shoot")) shoot(args[1]);
			else if (args[0].equals("testshoot")) testShoot(args[1]);
			else sendError("Unknown Command!");
		} else sendError("Unknown Command!");
	}
	
	private void shoot(String waypointName) {
		Entity target = ApiMcheliBvr.instance.getPingEntity(user.getDisplayName(), waypointName);
		if (target == null) {
			sendError("You are not locked onto that ping!");
			return;
		}
		if (!(target instanceof MCH_EntityAircraft)) {
			sendError("You can only fire missiles at Mcheli Aircraft!");
			return;
		}
		if (ApiMcheliBvr.instance.launchMcheliMissile(user, (MCH_EntityAircraft)target)) sendMessage("Missile Fired!");
	}
	
	private void testShoot(String name) {
		Entity target = user.getEntityWorld().getPlayerEntityByName(name);
		if (target == null) {
			sendError("That Player Doesn't Exist");
			return;
		}
		ApiMcheliBvr.instance.launchTestMissile(user, target);
		sendMessage("Missile Fired!");
	}
	
	private String[] getPingNames(String playerName) {
		return ApiMcheliBvr.instance.getPlayerPingNames(playerName);
	}
	
	private void sendMessage(String message) {
		if (user == null) return;
		ChatComponentText chat = new ChatComponentText(message);
		ChatStyle style = new ChatStyle();
		style.setColor(EnumChatFormatting.YELLOW);
		chat.setChatStyle(style);
		user.addChatMessage(chat);
	}
	
	private void sendError(String message) {
		if (user == null) return;
		ChatComponentText chat = new ChatComponentText(message);
		ChatStyle style = new ChatStyle();
		style.setColor(EnumChatFormatting.RED);
		chat.setChatStyle(style);
		user.addChatMessage(chat);
	}
}
