package me.stella.Services;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import me.stella.Plugin.LockerPlugin;

public class DiscordWebhookServices {
	
	public static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
	
	private LockerPlugin plugin;
	
	private boolean async;
	
	private String webhook;
	
	public DiscordWebhookServices(LockerPlugin main, boolean async, String webhookURL) {
		this.plugin = main;
		this.async = async;
		this.webhook = webhookURL;
	}
	
	public boolean isAsync() {
		return this.async;
	}
	
	public String getWebhook() {
		return this.webhook;
	}
	
	public synchronized void postToWebhook(JSONObject payload) {
		BukkitRunnable runnable = (new BukkitRunnable() {
			@Override
			public void run() {
				try {
					URL webhookURL = new URL(getWebhook());
					HttpURLConnection webhookConnection = (HttpURLConnection) webhookURL.openConnection();
					webhookConnection.setRequestMethod("POST");
					webhookConnection.setRequestProperty("Content-Type", "application/json; utf-8");
					webhookConnection.setRequestProperty("Accept", "application/json");
					webhookConnection.setDoOutput(true);
					OutputStream output = webhookConnection.getOutputStream();
					byte[] byteArrayPayload = payload.toJSONString().getBytes(StandardCharsets.UTF_8);
					output.write(byteArrayPayload, 0, byteArrayPayload.length); 
					output.flush(); output.close();
					webhookConnection.getResponseCode(); webhookConnection.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		if(isAsync())
			runnable.runTaskAsynchronously(plugin);
		else
			runnable.runTask(plugin);
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject buildInfoPayload(String player, JSONObject responsePayload) {
		String regionCode = responsePayload.get("regionName") + "," + responsePayload.get("country");
		JSONObject payloadBuilder = new JSONObject();
		payloadBuilder.put("content", "");
		payloadBuilder.put("tts", false);
		payloadBuilder.put("embeds", buildInfoBody(player, regionCode, String.valueOf(responsePayload.get("isp")), String.valueOf(responsePayload.get("as")),
				(boolean)responsePayload.get("hosting"), (boolean)responsePayload.get("proxy"), (boolean)responsePayload.get("mobile")));
		return payloadBuilder;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject buildErrorPayload(String player, String error) {
		JSONObject payloadBuilder = new JSONObject();
		payloadBuilder.put("content", "");
		payloadBuilder.put("tts", false);
		payloadBuilder.put("embeds", buildErrorBody(player, error));
		return payloadBuilder;
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray buildInfoBody(String player, String region, String isp, String as, boolean hosting, boolean proxy, boolean mobile) {
		JSONArray embeds = new JSONArray();
		JSONObject embedHeader = new JSONObject();
		embedHeader.put("type", "rich");
		embedHeader.put("title", "AddressLocker - Phát hiện IP không hợp lệ!");
		embedHeader.put("description", "Người chơi **" + player + "**");
		embedHeader.put("color", 0x13db35);
		JSONArray fieldArray = new JSONArray();
		fieldArray.add(buildField("Đăng nhập lúc", buildCurrentTime(), true));
		fieldArray.add(buildField("Địa điểm", region, true));
		fieldArray.add(buildField("Tên nhà mạng", isp, false));
		fieldArray.add(buildField("AS", as, false));
		fieldArray.add(buildField("Hosted IP", hosting, true));
		fieldArray.add(buildField("Proxy/VPN", proxy, true));
		fieldArray.add(buildField("Mobile", mobile, true));
		embedHeader.put("fields", fieldArray); embeds.add(embedHeader);
		return embeds;
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray buildErrorBody(String player, String error) {
		JSONArray embeds = new JSONArray();
		JSONObject embedHeader = new JSONObject();
		embedHeader.put("type", "rich");
		embedHeader.put("title", "AddressLocker - Phát hiện IP không hợp lệ!");
		embedHeader.put("description", "Người chơi **" + player + "**");
		embedHeader.put("color", 0xdb1111);
		JSONArray fieldArray = new JSONArray();
		fieldArray.add(buildField("Đăng nhập lúc", buildCurrentTime(), true));
		fieldArray.add(buildField("Ghi chú lỗi", error, true));
		embedHeader.put("fields", fieldArray); embeds.add(embedHeader);
		return embeds;
	}
	
	private String buildCurrentTime() {
		return DiscordWebhookServices.format.format(Calendar.getInstance().getTime());
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject buildField(String name, Object value, boolean inLine) {
		JSONObject paramObject = new JSONObject();
		paramObject.put("name", name);
		paramObject.put("value", value);
		paramObject.put("inline", inLine);
		return paramObject;
	}

}
