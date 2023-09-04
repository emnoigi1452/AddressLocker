package me.stella.Services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.parser.JSONParser;

import me.stella.Handlers.AddressHandle;
import me.stella.Plugin.LockerPlugin;

public class APIRequestService {
	
	public static final String API = "http://ip-api.com/json/{address}?fields=17025551";
	
	private int requestCounter = 0;
	
	private int timer = 0;
	
	private Map<String, String> pending;
	
	private List<String> pendingNames;
	
	private LockerPlugin plugin;
	
	public APIRequestService(LockerPlugin main, long interval) {
		this.plugin = main;
		this.requestCounter = 0;
		this.timer = 0;
		this.pending = Collections.synchronizedMap(new HashMap<>());
		this.pendingNames = Collections.synchronizedList(new ArrayList<>());
		(new BukkitRunnable() {
			@Override
			public void run() {
				timer++;
				if(timer >= 60) {
					timer = 0;
					requestCounter = 0;
				}
			}
		}).runTaskTimerAsynchronously(plugin, 20L, 20L);
		(new BukkitRunnable() {
			@Override
			public void run() {
				if(requestCounter <= 35) {
					if(pendingNames.size() > 0) {
						String requestName = pendingNames.get(0);
						String requestIP = pending.get(requestName);
						pending.remove(requestName);
						pendingNames.remove(0);
						requestCounter++;
						AddressHandle.play(requestName, requestIP);
					}
				}
			}
		}).runTaskTimerAsynchronously(main, 0L, interval);
	}
	
	public synchronized void addToIPCheck(String player, String ip) {
		this.pending.put(player, ip);
		this.pendingNames.add(player);
	}
	
	public LockerPlugin getMain() {
		return this.plugin;
	}
	
	public int getSentRequests() {
		return this.requestCounter;
	}
	
	public int getTimer() {
		return this.timer;
	}
	
	public static CompletableFuture<Object> postIPRequest(String ip) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				URL requestURL = new URL(APIRequestService.API.replace("{address}", ip));
				HttpURLConnection apiConnection = (HttpURLConnection) requestURL.openConnection();
				InputStream apiResponse = apiConnection.getInputStream();
				BufferedReader responseReader = new BufferedReader(new InputStreamReader(apiResponse));
				StringBuilder responseBuilder = new StringBuilder(); String buffer;
				while((buffer = responseReader.readLine()) != null)
					responseBuilder.append(buffer);
				responseReader.close(); apiResponse.close();
				apiConnection.disconnect();
				return (new JSONParser()).parse(responseBuilder.toString());
			} catch(Exception err) { err.printStackTrace(); }
			return null;
		});
	}

}
