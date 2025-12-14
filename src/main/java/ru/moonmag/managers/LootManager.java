package ru.moonmag.managers;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.moonmag.SpaceChest;
import ru.moonmag.models.LootItem;
import ru.moonmag.models.LootTable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class LootManager {
    private final SpaceChest plugin;
    private final Map<String, LootTable> lootTables = new HashMap<>();
    private File lootFile;

    public LootManager(SpaceChest plugin) {
        this.plugin = plugin;
        this.lootFile = new File(plugin.getDataFolder(), "loot.yml");
    }

    public void loadLootTables() {
        lootTables.clear();

        if (!lootFile.exists()) {
            try {
                lootFile.createNewFile();
            } catch (IOException e) {
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(lootFile);

        for (String tableName : config.getKeys(false)) {
            LootTable table = new LootTable(tableName);
            ConfigurationSection itemsSection = config.getConfigurationSection(tableName);

            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    double chance = itemsSection.getDouble(key + ".chance");

                    if (itemsSection.contains(key + ".item")) {
                        ItemStack item = itemsSection.getItemStack(key + ".item");
                        if (item != null) {
                            table.addItem(new LootItem(item, chance));
                        }
                    }
                }
            }
            lootTables.put(tableName, table);
        }
    }

    public void saveLootTable(String tableName, LootTable table) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(lootFile);

        config.set(tableName, null);

        if (table.getItems().isEmpty()) {
            config.createSection(tableName);
        } else {
            int index = 0;
            for (LootItem item : table.getItems()) {
                String path = tableName + "." + index;
                config.set(path + ".item", item.getItemStack());
                config.set(path + ".chance", item.getChance());
                index++;
            }
        }

        try {
            config.save(lootFile);
            lootTables.put(tableName, table);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteLootTable(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(lootFile);
        config.set(tableName, null);

        try {
            config.save(lootFile);
            lootTables.remove(tableName);
        } catch (IOException e) {
        }
    }

    public void fillChest(Location loc, String tableName) {
        Block block = loc.getBlock();

        if (!(block.getState() instanceof org.bukkit.block.Container)) {
            return;
        }

        org.bukkit.block.Container container = (org.bukkit.block.Container) block.getState();
        Inventory inv = container.getInventory();
        inv.clear();

        LootTable table = lootTables.get(tableName);
        if (table == null) return;

        List<ItemStack> droppedItems = new ArrayList<>();
        Random random = new Random();

        for (LootItem item : table.getItems()) {
            if (random.nextDouble() * 100 < item.getChance()) {
                droppedItems.add(item.getItemStack().clone());
            }
        }

        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) {
            availableSlots.add(i);
        }

        for (ItemStack lootItem : droppedItems) {
            if (availableSlots.isEmpty()) break;

            int randomIndex = random.nextInt(availableSlots.size());
            int slot = availableSlots.remove(randomIndex);
            inv.setItem(slot, lootItem);
        }
    }

    public LootTable getLootTable(String name) {
        return lootTables.get(name);
    }

    public Map<String, LootTable> getLootTables() {
        return lootTables;
    }
}