package me.stella.Handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.stella.Plugin.LockerPlugin;

public class RequestCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equals("request")) {
			if(sender instanceof Player)
				sender.sendMessage(LockerPlugin.color("&cThis command is reserved for console only!"));
			else {
				String target;
				try {
					target = args[0];
				} catch(Throwable t) { sender.sendMessage("/request <player>"); return false; }
				String ip = LoginHandle.ipMap.getOrDefault(target, "");
				if(ip.equals(""))
					sender.sendMessage(LockerPlugin.color("&cPlayer does not exist! Please check again..."));
				else LockerPlugin.main.getRequestService().addToIPCheck(target, ip);
			}
		}
		return true;
	}

}
