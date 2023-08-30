package me.stella.Services;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.stella.Plugin.LockerPlugin;

public class BungeeCordServices {
	
	private LockerPlugin plugin;
	private boolean async;
	
	public BungeeCordServices(LockerPlugin main, boolean async) {
		this.plugin = main;
		this.async = async;
	}
	
	public boolean isAsync() {
		return this.async;
	}
	
	public void kickPlayer(final String name, final BungeeKickMode mode) {
		String coloredMessage = LockerPlugin.color(
				LockerPlugin.main.getLockerSettings().getConfig().getString("handle.block.message"));
		switch(mode) {
			case LOBBY:
				messageBungeeCord(isAsync(), mode.getExecuteDelay(), new String[] { "ConnectOther", name, mode.getBungeeNode() });
				sendMessage(name, coloredMessage); break;
			case NETWORK:
				messageBungeeCord(isAsync(), mode.getExecuteDelay(), new String[] { "KickPlayer", name, coloredMessage });
				break;
			default:
				break;
		}
	}
	
	public void sendMessage(final String target, String message) {
		messageBungeeCord(isAsync(), 0L, new String[] { "Message", target, message });
	}
	
	private void messageBungeeCord(boolean async, long delay, final String... params) {
		BukkitRunnable runnable = (new BukkitRunnable() {
			@Override
			public void run() {
				ByteArrayDataOutput output = ByteStreams.newDataOutput();
				for(String param: params)
					output.writeUTF(param);
				Bukkit.getServer().sendPluginMessage(plugin, "BungeeCord", output.toByteArray());
			}
		});
		if(async)
			runnable.runTaskLaterAsynchronously(plugin, delay);
		else
			runnable.runTaskLater(plugin, delay);
	}
	
	public static enum BungeeKickMode {
		
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
