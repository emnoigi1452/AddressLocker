package me.stella.Services;

import me.stella.Plugin.LockerPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public class SoftEtherService {

    public static final Set<String> cache = new HashSet<>();

    @Deprecated
    public static void bootMapTask(boolean permanent, long delay) {
        (new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if(!permanent)
                        cache.clear();
                    URL vpngate = new URL("http://www.vpngate.net/api/iphone/");
                    HttpURLConnection connection = (HttpURLConnection) vpngate.openConnection();
                    connection.setUseCaches(false);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    List<String> proxies = new ArrayList<>(); String packet = "";
                    while((packet = reader.readLine()) != null) {
                        if(!(packet.startsWith("#") || packet.startsWith("*")))
                            proxies.add(packet);
                    }
                    reader.close(); connection.disconnect();
                    proxies.forEach(e -> {
                        String[] content = e.split(",");
                        if(content[6].equalsIgnoreCase("VN")) {
                            cache.add(content[1]);
                        }
                    });
                } catch(Exception err) { err.printStackTrace(); }
            }
        }).runTaskTimerAsynchronously(LockerPlugin.main, 0L, delay);
    }



}
