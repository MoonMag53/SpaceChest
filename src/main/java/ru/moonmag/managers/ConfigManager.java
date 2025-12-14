package ru.moonmag.managers;

import org.bukkit.configuration.file.FileConfiguration;
import ru.moonmag.SpaceChest;
import ru.moonmag.utils.Hex;

public class ConfigManager {
    private final SpaceChest plugin;
    private FileConfiguration config;

    public ConfigManager(SpaceChest plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public String getMessage(String path) {
        String msg = config.getString("messages." + path, "&cСообщение не найдено!");
        return Hex.colorize(msg);
    }

    public String getMessage(String path, String placeholder, String value) {
        return getMessage(path).replace(placeholder, value);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}