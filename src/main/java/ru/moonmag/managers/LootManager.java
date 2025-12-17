package ru.moonmag.managers;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.moonmag.SpaceChest;
import ru.moonmag.models.LootItem;
import ru.moonmag.models.LootTable;
import java.util.*;

public class LootManager {
    private final SpaceChest plugin;
    private final Map<String, LootTable> lootTables = new HashMap<>();

    public LootManager(SpaceChest plugin) {
        this.plugin = plugin;
    }

    public void loadLootTables() {
        lootTables.clear();

        Map<String, LootTable> loadedTables = plugin.getDatabaseManager().loadLootTables();
        lootTables.putAll(loadedTables);
    }

    public void saveLootTable(String tableName, LootTable table) {
        if (tableName == null || tableName.trim().isEmpty()) {
            return;
        }

        plugin.getDatabaseManager().ensureConnection();
        plugin.getDatabaseManager().saveLootTable(tableName, table);

        lootTables.put(tableName, table);
    }

    public void deleteLootTable(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) return;

        plugin.getDatabaseManager().ensureConnection();
        plugin.getDatabaseManager().deleteLootTable(tableName);

        lootTables.remove(tableName);
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
        if (table == null) {
            plugin.getDatabaseManager().ensureConnection();
            table = plugin.getDatabaseManager().loadLootTable(tableName);
            if (table != null) {
                lootTables.put(tableName, table);
            } else {
                return;
            }
        }

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
        LootTable table = lootTables.get(name);
        if (table == null) {
            plugin.getDatabaseManager().ensureConnection();
            table = plugin.getDatabaseManager().loadLootTable(name);
            if (table != null) {
                lootTables.put(name, table);
            }
        }
        return table;
    }

    public Map<String, LootTable> getLootTables() {
        return lootTables;
    }
}