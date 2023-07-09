package me.stella.Plugin;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LockerConfig {
	
	private File file;
	private FileConfiguration config;
	
	public LockerConfig(File file) {
		load(file);
	}
	
	public void load(File file) {
		this.file = file.getAbsoluteFile();
		this.config = YamlConfiguration.loadConfiguration(file);
	}
	
	public File getFile() {
		return this.file.getAbsoluteFile();
	}
	
	public FileConfiguration getConfig() {
		return this.config;
	}
	
	public void save() {
		try {
			getConfig().save(getFile());
		} catch(Exception err) { err.printStackTrace(); }
	}

}
