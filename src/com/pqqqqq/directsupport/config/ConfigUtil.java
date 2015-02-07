package com.pqqqqq.directsupport.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigUtil {

	public static String getString(YamlConfiguration cfg, File file, String path, String def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, def);
			return def;
		}
		return cfg.getString(path, def);
	}

	public static int getInt(YamlConfiguration cfg, File file, String path, int def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, def);
			return def;
		}
		return cfg.getInt(path, def);
	}

	public static float getFloat(YamlConfiguration cfg, File file, String path, float def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, def);
			return def;
		}
		return cfg.getLong(path, (long) def);
	}

	public static boolean getBoolean(YamlConfiguration cfg, File file, String path, boolean def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, def);
			return def;
		}
		return cfg.getBoolean(path, def);
	}

	public static List<String> getStringList(YamlConfiguration cfg, File file, String path, String... def) {
		if (!cfg.isSet(path)) {
			cfg.set(path, new ArrayList<String>(Arrays.asList(def)));

			return new ArrayList<String>(Arrays.asList(def));
		}
		return cfg.getStringList(path);
	}
}
