package me.stella.Handlers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.stella.Plugin.LockerPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class LoginHandle implements Listener {
	
	public static final Map<String, String> ipMap = new HashMap<>();
	public static final Map<String, Integer> janitors = new HashMap<>();
	
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
				BukkitScheduler serverScheduler = Bukkit.getServer().getScheduler();
				if(LoginHandle.janitors.containsKey(accountName)) {
					int varint = LoginHandle.janitors.get(accountName);
					if(serverScheduler.isCurrentlyRunning(varint) || serverScheduler.isQueued(varint))
						serverScheduler.cancelTask(varint);
				}
				LoginHandle.ipMap.put(accountName, ip);
			}
		}).runTaskAsynchronously(plugin);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void logout(PlayerQuitEvent e) {
		final String accountName = e.getPlayer().getName();
		BukkitTask janitorTask = (new BukkitRunnable() {
			@Override
			public void run() {
				LoginHandle.ipMap.remove(accountName);
				LoginHandle.janitors.remove(accountName);
			}
		}).runTaskLaterAsynchronously(plugin, this.cleanupDelay);
		janitors.put(accountName, janitorTask.getTaskId());
	}

}
