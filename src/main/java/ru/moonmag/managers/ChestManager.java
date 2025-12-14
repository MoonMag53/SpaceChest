package ru.moonmag.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;
import ru.moonmag.SpaceChest;
import ru.moonmag.models.SpaceChestData;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChestManager {

    private final SpaceChest plugin;
    private final Map<String, SpaceChestData> chests = new HashMap<>();
    private final Map<String, BukkitTask> reloadTasks = new HashMap<>();
    private final Map<String, String> locationMap = new HashMap<>();

    public ChestManager(SpaceChest plugin) {
        this.plugin = plugin;
    }

    public void loadChests() {
        reloadTasks.values().forEach(BukkitTask::cancel);
        reloadTasks.clear();

        chests.clear();
        locationMap.clear();

        ConfigurationSection chestsSection = plugin.getConfig().getConfigurationSection("chests");
        if (chestsSection == null) {
            cleanupOrphanedLootTables(new HashSet<>());
            return;
        }

        Set<String> validChestIds = new HashSet<>();

        for (String id : chestsSection.getKeys(false)) {
            ConfigurationSection chestConfig = chestsSection.getConfigurationSection(id);
            if (chestConfig == null) continue;

            String worldName = chestConfig.getString("world");
            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                continue;
            }

            Location loc = new Location(
                    world,
                    chestConfig.getInt("x"),
                    chestConfig.getInt("y"),
                    chestConfig.getInt("z")
            );

            Block block = loc.getBlock();

            Material blockType;
            try {
                blockType = Material.valueOf(chestConfig.getString("block-type"));
            } catch (IllegalArgumentException e) {
                blockType = Material.CHEST;
            }

            if (block.getType() == Material.AIR || block.getType() != blockType) {
                block.setType(blockType);
            }

            int reloadInterval = chestConfig.getInt("reload-interval");

            SpaceChestData data = new SpaceChestData(
                    id,
                    loc,
                    blockType,
                    reloadInterval,
                    id
            );

            chests.put(id, data);
            locationMap.put(getLocationKey(loc), id);
            validChestIds.add(id);

            startReloadTimer(data);
            plugin.getHologramManager().createHologram(data);
        }

        cleanupOrphanedLootTables(validChestIds);
    }

    private void cleanupOrphanedLootTables(Set<String> validChestIds) {
        Set<String> lootTableNames = new HashSet<>(plugin.getLootManager().getLootTables().keySet());

        for (String lootTableName : lootTableNames) {
            if (!validChestIds.contains(lootTableName)) {
                plugin.getLootManager().deleteLootTable(lootTableName);
            }
        }
    }

    private void startReloadTimer(SpaceChestData data) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            data.decrementTimer();

            if (data.getTimeLeft() <= 0) {
                reloadChest(data);
                data.resetTimer();
            }

            plugin.getHologramManager().updateHologram(data);
        }, 20L, 20L);

        reloadTasks.put(data.getId(), task);
    }

    private void reloadChest(SpaceChestData data) {
        plugin.getLootManager().fillChest(data.getLocation(), data.getLootTable());
    }

    public SpaceChestData getChestByLocation(Location loc) {
        return chests.get(locationMap.get(getLocationKey(loc)));
    }

    public Map<String, SpaceChestData> getChests() {
        return chests;
    }

    private String getLocationKey(Location loc) {
        return loc.getWorld().getName() + ":" +
                loc.getBlockX() + ":" +
                loc.getBlockY() + ":" +
                loc.getBlockZ();
    }

    public void shutdown() {
        reloadTasks.values().forEach(BukkitTask::cancel);
        reloadTasks.clear();
    }
}