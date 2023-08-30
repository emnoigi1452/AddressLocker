package me.stella.Handlers;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;

import me.stella.Plugin.LockerConfig;
import me.stella.Plugin.LockerPlugin;
import me.stella.Services.APIRequestService;
import me.stella.Services.BungeeCordServices;
import me.stella.Services.BungeeCordServices.BungeeKickMode;
import me.stella.Services.DiscordWebhookServices;

public class AddressHandle {

	public final static Set<String> monitor = new HashSet<>();
	public final static Set<String> queries = new HashSet<>();
	public final static Set<String> bannedProviders = new HashSet<>();
	public static boolean checkRegion;
	public static boolean checkHost;
	public static boolean checkProxy;
	public static boolean checkMobile;
	public static boolean checkAS;
	
	public static void play(final String player, final String ip) {
		(new BukkitRunnable() {
			@Override
			public void run() {
				if(monitor.contains(player))
					this.cancel();
				monitor.add(player);
				final LockerConfig handleConfig = LockerPlugin.main.getLockerSettings();
				final BungeeCordServices bungeeService = LockerPlugin.main.getBungeeService();
				final DiscordWebhookServices webhookService = LockerPlugin.main.getDiscordService();
				try {
					CompletableFuture<Object> requestObject = APIRequestService.postIPRequest(ip);
					requestObject.thenAcceptAsync(object -> {
						JSONObject response = (JSONObject) object;
						LockerPlugin.console.log(Level.INFO, ChatColor.GREEN + player + " - " + response.toJSONString());
						if (String.valueOf(response.get("status")).equals("fail")) {
							if (handleConfig.getConfig().getBoolean("handle.discord.enabled")) {
								JSONObject errorPayload = webhookService.buildErrorPayload(player, String.valueOf(response.get("message")));
								webhookService.postToWebhook(errorPayload);
							}
							if (handleConfig.getConfig().getBoolean("handle.warning.enabled")) {
								String errorTrace = handleConfig.getConfig().getString("handle.warning.message-error")
										.replace("{name}", player).replace("{error}", String.valueOf(response.get("message")));
								for (String staffMember : handleConfig.getConfig().getStringList("handle.warning.staff"))
									bungeeService.sendMessage(staffMember, LockerPlugin.color(errorTrace));
							}
							if (handleConfig.getConfig().getBoolean("handle.kick-error-player")) {
								BungeeKickMode kickMode = null;
								try {
									kickMode = BungeeKickMode.valueOf("handle.block.kick-mode");
								} catch (Exception err) {
									kickMode = BungeeKickMode.LOBBY;
								}
								bungeeService.kickPlayer(player, kickMode);
							}
							return;
						}
						// check for when the ip is sus
						if (AddressHandle.badIP(AddressHandle.serializeNetworkProperty(response))) {
							if (handleConfig.getConfig().getBoolean("handle.block.enabled")) {
								BungeeKickMode kickMode;
								try {
									kickMode = BungeeKickMode.valueOf(handleConfig.getConfig().getString("handle.block.kick-mode"));
								} catch (Exception err) {
									kickMode = BungeeKickMode.LOBBY;
								}
								bungeeService.kickPlayer(player, kickMode);
							}
							if (handleConfig.getConfig().getBoolean("handle.discord.enabled")) {
								JSONObject infoPayload = webhookService.buildInfoPayload(player, response);
								webhookService.postToWebhook(infoPayload);
							}
							if (handleConfig.getConfig().getBoolean("handle.warning.enabled")) {
								String regionInfo = response.get("regionName") + "," + response.get("country");
								String errorTrace = handleConfig.getConfig().getString("handle.warning.message")
										.replace("{name}", player).replace("{region}", regionInfo);
								for (String staffMember : handleConfig.getConfig().getStringList("handle.warning.staff"))
									bungeeService.sendMessage(staffMember, LockerPlugin.color(errorTrace));
							}
						}
					});
				} catch(Exception err) { err.printStackTrace(); }
			}
		}).runTaskAsynchronously(LockerPlugin.main);
	}

	protected static Object[] serializeNetworkProperty(JSONObject jsonObject) {
		return (new Object[] {
				jsonObject.get("countryCode"),
				jsonObject.get("hosting"),
				jsonObject.get("proxy"),
				jsonObject.get("isp"),
				jsonObject.get("as"),
				jsonObject.get("mobile")
		});
	}
	
	public static boolean badIP(Object[] packet) {
		String country = String.valueOf(packet[0]).trim();
		boolean hosting = ((boolean) packet[1]);
		boolean proxy = ((boolean) packet[2]);
		String provider = String.valueOf(packet[3]);
		String as = String.valueOf(packet[4]);
		boolean mobile = ((boolean) packet[5]);
		boolean flagStatus = false;
		if(checkRegion)
			flagStatus = flagStatus || (!(country.equals("VN")));
		if((!flagStatus) && checkHost)
			flagStatus = flagStatus || hosting;
		if((!flagStatus) && checkProxy) {
			if(proxy)
				flagStatus = flagStatus || (AddressHandle.isProviderBanned(provider));
		}
		if(!flagStatus && checkAS) {
			boolean safe = true;
			for(String card: AddressHandle.queries)
				if(as.contains(card)) {
					safe = false;
					break;
				}
			flagStatus = flagStatus || !safe;
		}
		if((!flagStatus) && checkMobile)
			flagStatus = flagStatus || mobile;
		return flagStatus;
	}
	
	public static boolean isProviderBanned(String provider) {
		if(AddressHandle.bannedProviders.size() == 0)
			return true;
		return AddressHandle.bannedProviders.contains(provider);
	}
}
