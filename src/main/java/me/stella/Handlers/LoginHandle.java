package me.stella.Handlers;

import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.stella.Plugin.LockerPlugin;

public class LoginHandle implements Listener {
	
	public static Map<String, String> ipMap;
	
	private LockerPlugin plugin;
	
	private long cleanupDelay;
	
	public LoginHandle(LockerPlugin main, long cacheCleanup) {
		this.plugin = main;
		this.cleanupDelay = cacheCleanup;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void login(AsyncPlayerPreLoginEvent e) {
		String accountName = e.getName();
		AddressHandle.monitor.remove(accountName);
		String ip = e.getAddress().getHostAddress();
		(new BukkitRunnable() {
			@Override
			public void run() {
				LoginHandle.ipMap.put(accountName, ip);
			}
		}).runTaskAsynchronously(plugin);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void logout(PlayerQuitEvent e) {
		String accountName = e.getPlayer().getName();
		(new BukkitRunnable() {
			@Override
			public void run() {
				LoginHandle.ipMap.remove(accountName);
			}
		}).runTaskLaterAsynchronously(plugin, this.cleanupDelay);
	}

}
