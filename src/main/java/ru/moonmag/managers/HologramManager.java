package ru.moonmag.managers;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import ru.moonmag.SpaceChest;
import ru.moonmag.models.SpaceChestData;
import ru.moonmag.utils.Hex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramManager {
    private final SpaceChest plugin;
    private final Map<String, Hologram> holograms = new HashMap<>();

    public HologramManager(SpaceChest plugin) {
        this.plugin = plugin;
    }

    public void createHologram(SpaceChestData data) {
        removeHologram(data.getId());

        ConfigurationSection holoConfig = plugin.getConfig()
                .getConfigurationSection("chests." + data.getId() + ".hologram");

        if (holoConfig == null) return;

        double offsetY = holoConfig.getDouble("offset-y");

        Location blockLoc = data.getLocation();
        Location holoLoc = new Location(
                blockLoc.getWorld(),
                blockLoc.getBlockX() + 0.5,
                blockLoc.getBlockY() + offsetY,
                blockLoc.getBlockZ() + 0.5
        );

        List<String> lines = holoConfig.getStringList("lines");
        List<String> coloredLines = new ArrayList<>();

        for (String line : lines) {
            coloredLines.add(Hex.colorize(line));
        }

        Hologram existingHologram = DHAPI.getHologram(data.getId());
        if (existingHologram != null) {
            existingHologram.delete();
        }

        Hologram hologram = DHAPI.createHologram(data.getId(), holoLoc, coloredLines);
        holograms.put(data.getId(), hologram);
    }

    public void removeHologram(String id) {
        Hologram hologram = holograms.remove(id);
        if (hologram != null) {
            hologram.delete();
        }

        Hologram dhHologram = DHAPI.getHologram(id);
        if (dhHologram != null) {
            dhHologram.delete();
        }
    }

    public void updateHologram(SpaceChestData data) {
        Hologram hologram = holograms.get(data.getId());
        if (hologram == null) return;

        ConfigurationSection holoConfig = plugin.getConfig()
                .getConfigurationSection("chests." + data.getId() + ".hologram");

        if (holoConfig == null) return;

        List<String> lines = holoConfig.getStringList("lines");
        String timeFormatted = formatTime(data.getTimeLeft());

        for (int i = 0; i < lines.size(); i++) {
            String line = Hex.colorize(lines.get(i).replace("%time%", timeFormatted));
            DHAPI.setHologramLine(hologram, i, line);
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    public void removeAllHolograms() {
        for (String id : new ArrayList<>(holograms.keySet())) {
            Hologram hologram = holograms.get(id);
            if (hologram != null) {
                hologram.delete();
            }
        }
        holograms.clear();

        ConfigurationSection chestsSection = plugin.getConfig().getConfigurationSection("chests");
        if (chestsSection != null) {
            for (String id : chestsSection.getKeys(false)) {
                Hologram dhHologram = DHAPI.getHologram(id);
                if (dhHologram != null) {
                    dhHologram.delete();
                }
            }
        }
    }
}