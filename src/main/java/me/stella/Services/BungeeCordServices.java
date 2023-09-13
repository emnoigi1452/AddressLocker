package me.stella.Services;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.stella.Plugin.LockerPlugin;

public class BungeeCordServices {
	
	private LockerPlugin plugin;
	
	public BungeeCordServices(LockerPlugin main) {
		this.plugin = main;
	}
	
	public void kickPlayer(final String name, final BungeeKickMode mode) {
		String coloredMessage = LockerPlugin.color(
				LockerPlugin.main.getLockerSettings().getConfig().getString("handle.block.message"));
		switch(mode) {
			case LOBBY:
				messageBungeeCord(mode.getExecuteDelay(), new String[] { "ConnectOther", name, mode.getBungeeNode() });
				sendMessage(name, coloredMessage); break;
			case NETWORK:
				messageBungeeCord(mode.getExecuteDelay(), new String[] { "KickPlayer", name, coloredMessage });
				break;
			default:
				break;
		}
	}
	
	public void sendMessage(final String target, String message) {
		messageBungeeCord(0L, new String[] { "Message", target, message });
	}
	
	private void messageBungeeCord(long delay, final String... params) {
		(new BukkitRunnable() {
			@Override
			public void run() {
				ByteArrayDataOutput output = ByteStreams.newDataOutput();
				for(String param: params)
					output.writeUTF(param);
				Bukkit.getServer().sendPluginMessage(plugin, "BungeeCord", output.toByteArray());
			}
		}).runTask(LockerPlugin.main);
	}
	
	public enum BungeeKickMode {
		
		LOBBY(0, LockerPlugin.main.getLockerSettings().getConfig().getString("handle.block.lobby-server")),
		NETWORK(20, null);
		
		private long delay;
		private String serverNode;
		
		BungeeKickMode(long delay, String serverNode) {
			this.delay = delay;
			this.serverNode = serverNode;
		}
		
		public long getExecuteDelay() {
			return this.delay;
		}
		
		public String getBungeeNode() {
			return this.serverNode;
		}
		
	}

}
