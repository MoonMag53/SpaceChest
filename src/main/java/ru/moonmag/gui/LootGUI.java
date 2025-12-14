package ru.moonmag.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.moonmag.SpaceChest;
import ru.moonmag.models.LootItem;
import ru.moonmag.models.LootTable;
import ru.moonmag.utils.Hex;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LootGUI implements Listener {

    private final SpaceChest plugin;
    private final Player player;
    private final LootTable table;
    private final Inventory inv;
    private final String title;

    public LootGUI(SpaceChest plugin, Player player, LootTable table) {
        this.plugin = plugin;
        this.player = player;
        this.table = table;

        this.title = Hex.colorize(
                plugin.getConfig().getString("gui.title")
                        .replace("%loot%", table.getName())
        );

        int size = plugin.getConfig().getInt("gui.size");
        this.inv = Bukkit.createInventory(null, size, title);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        refresh();
    }

    public void open() {
        player.openInventory(inv);
    }

    private void refresh() {
        inv.clear();
        int slot = 0;

        for (LootItem item : table.getItems()) {
            if (slot >= inv.getSize()) break;

            ItemStack stack = item.getItemStack().clone();
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) continue;

            List<String> lore = new ArrayList<>();

            List<String> configLore = plugin.getConfig().getStringList("gui.item-lore");
            for (String line : configLore) {
                lore.add(Hex.colorize(line.replace("%chance%", String.valueOf(item.getChance()))));
            }

            meta.setLore(lore);
            stack.setItemMeta(meta);

            inv.setItem(slot++, stack);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(title)) return;

        ItemStack clicked = e.getCurrentItem();
        ItemStack cursor = e.getCursor();
        ClickType click = e.getClick();

        if (e.getRawSlot() < inv.getSize()) {
            e.setCancelled(true);

            if ((clicked == null || clicked.getType() == Material.AIR) && cursor != null && cursor.getType() != Material.AIR) {
                ItemStack toAdd = cursor.clone();
                table.addItem(new LootItem(toAdd, 50.0));
                e.getWhoClicked().setItemOnCursor(null);
                plugin.getLootManager().saveLootTable(table.getName(), table);
                refresh();
                return;
            }

            if (clicked == null || clicked.getType() == Material.AIR) return;

            LootItem lootItem = getLootItem(e.getRawSlot());
            if (lootItem == null) return;

            switch (click) {
                case LEFT -> lootItem.setChance(lootItem.getChance() + 1);
                case RIGHT -> lootItem.setChance(lootItem.getChance() - 1);
                case SHIFT_LEFT -> lootItem.setChance(lootItem.getChance() + 10);
                case SHIFT_RIGHT -> lootItem.setChance(lootItem.getChance() - 10);

                case MIDDLE -> {
                    ItemStack clean = lootItem.getItemStack().clone();
                    ItemMeta meta = clean.getItemMeta();
                    if (meta != null) {
                        meta.setLore(null);
                        meta.setDisplayName(null);
                        clean.setItemMeta(meta);
                    }

                    Map<Integer, ItemStack> left = p.getInventory().addItem(clean);
                    if (left.isEmpty()) {
                        table.removeItem(lootItem);
                    }
                }
            }

            plugin.getLootManager().saveLootTable(table.getName(), table);
            refresh();
            return;
        }

        if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
            e.setCancelled(true);
            if (clicked == null || clicked.getType() == Material.AIR) return;

            ItemStack toAdd;

            if (clicked.getAmount() > 1) {
                toAdd = clicked.clone();
                e.setCurrentItem(null);
            } else {
                toAdd = clicked.clone();
                toAdd.setAmount(1);
                e.setCurrentItem(null);
            }

            table.addItem(new LootItem(toAdd, 50.0));

            plugin.getLootManager().saveLootTable(table.getName(), table);
            refresh();
            return;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!e.getView().getTitle().equals(title)) return;
        if (!(e.getPlayer() instanceof Player p)) return;

        plugin.getLootManager().saveLootTable(table.getName(), table);

        String saveMessage = plugin.getConfigManager().getMessage("loot-saved");
        if (saveMessage != null && !saveMessage.isEmpty()) {
            p.sendMessage(Hex.colorize(saveMessage));
        }

        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }

    private LootItem getLootItem(int slot) {
        if (slot < 0 || slot >= table.getItems().size()) return null;
        return table.getItems().get(slot);
    }
}