package me.stella.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.stella.Services.SoftEtherService;
import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.stella.Handlers.AddressHandle;
import me.stella.Handlers.LoginHandle;
import me.stella.Handlers.RequestCommand;
import me.stella.Services.APIRequestService;
import me.stella.Services.BungeeCordServices;
import me.stella.Services.DiscordWebhookServices;
import net.md_5.bungee.api.ChatColor;

public class LockerPlugin extends JavaPlugin {
	
	public static Logger console = Logger.getLogger("Minecraft");
	
	public static LockerPlugin main;
	
	private LockerConfig config;
	private APIRequestService requestService;
	private BungeeCordServices bungeeService;
	private DiscordWebhookServices discordService;
	
	@Override
	public void onEnable() {
		main = this;
		saveDefaultConfig();
		this.config = new LockerConfig(new File(main.getDataFolder(), "config.yml"));
		console.log(Level.INFO, "Configuration loaded! Attemptng to load checks basic checks...");
		AddressHandle.checkRegion = this.config.getConfig().getBoolean("filter.check-vn");
		AddressHandle.checkHost = this.config.getConfig().getBoolean("filter.check-hosting");
		AddressHandle.checkProxy = this.config.getConfig().getBoolean("filter.check-proxy");
		AddressHandle.checkMobile = this.config.getConfig().getBoolean("filter.check-mobile");
		AddressHandle.checkAS = this.config.getConfig().getBoolean("filter.check-as");
		for(String provider: this.config.getConfig().getStringList("filter.banned-proxies"))
			AddressHandle.bannedProviders.add(provider);
		for(String wildcard: this.config.getConfig().getStringList("filter.wildcard"))
			AddressHandle.queries.add(wildcard);
		boolean enabled = this.config.getConfig().getBoolean("blacklist.softether.enabled", false);
		if(enabled) {
			SoftEtherService.enabled = true;
			long softEtherDelay = this.config.getConfig().getLong("blacklist.softether.update-interval", 3600L) * 20L;
			boolean permanentCache = this.config.getConfig().getBoolean("blacklist.softether.permanent-caching", false);
			SoftEtherService.bootMapTask(permanentCache, softEtherDelay);
			console.log(Level.INFO, "- SoftEther IP caching is enabled!");
		}
		console.log(Level.INFO, "Enabling other modules...");
		this.requestService = new APIRequestService(main, this.config.getConfig().getLong("handle.request-internal"));
		console.log(Level.INFO, " - API Request Service has been loaded!");
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		this.bungeeService = new BungeeCordServices(main);
		console.log(Level.INFO, " - BungeeCord Message Service has been loaded!");
		this.discordService = new DiscordWebhookServices(main, 
				this.config.getConfig().getBoolean("handle.async"), this.config.getConfig().getString("handle.discord.webhook"));
		console.log(Level.INFO, " - Discord Webhook Service has been loaded!");
		Bukkit.getServer().getPluginManager().registerEvents(
				new LoginHandle(main, this.config.getConfig().getLong("handle.cleanup-delay")), main);
		console.log(Level.INFO, " - All logging handlers are loaded!");
		main.getCommand("request").setExecutor(new RequestCommand());
		Bukkit.getServer().getOnlinePlayers().stream().forEach(online -> {
			String ip = online.getAddress().getAddress().getHostAddress();
			LoginHandle.ipMap.put(online.getName(), ip);
		});
	}
	
	@Override
	public void onDisable() {
		Bukkit.getServer().getScheduler().cancelTasks(main);
		AsyncPlayerPreLoginEvent.getHandlerList().unregister(main);
		PlayerQuitEvent.getHandlerList().unregister(main);
		Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
	}
	
	public LockerConfig getLockerSettings() {
		return this.config;
	}
	
	public APIRequestService getRequestService() {
		return this.requestService;
	}
	
	public BungeeCordServices getBungeeService() {
		return this.bungeeService;
	}
	
	public DiscordWebhookServices getDiscordService() {
		return this.discordService;
	}
	
	public static String color(String param) {
		return ChatColor.translateAlternateColorCodes('&', param);
	}

}
