package ru.moonmag.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import ru.moonmag.SpaceChest;
import ru.moonmag.models.SpaceChestData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChestListener implements Listener {
    private final SpaceChest plugin;
    private final Map<UUID, Long> lastTakeTime = new HashMap<>();
    private final Map<String, UUID> openedChests = new HashMap<>();

    public ChestListener(SpaceChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChestBreak(BlockBreakEvent e) {
        SpaceChestData data = plugin.getChestManager().getChestByLocation(e.getBlock().getLocation());
        if (data != null) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(plugin.getConfigManager().getMessage("chest-protected"));
        }
    }

    @EventHandler
    public void onChestOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;

        if (e.getInventory().getLocation() == null) return;

        SpaceChestData data = plugin.getChestManager().getChestByLocation(e.getInventory().getLocation());
        if (data != null) {
            openedChests.put(e.getInventory().hashCode() + "", player.getUniqueId());
        }
    }

    @EventHandler
    public void onChestClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;

        if (e.getInventory().getLocation() == null) return;

        SpaceChestData data = plugin.getChestManager().getChestByLocation(e.getInventory().getLocation());
        if (data != null) {
            openedChests.remove(e.getInventory().hashCode() + "");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        Inventory topInv = e.getView().getTopInventory();
        if (topInv == null || topInv.getLocation() == null) return;

        SpaceChestData data = plugin.getChestManager().getChestByLocation(topInv.getLocation());
        if (data == null) return;

        if (e.getRawSlot() < topInv.getSize() && e.getCursor() != null && !e.getCursor().getType().isAir()) {
            e.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("chest-no-place"));
            return;
        }

        if (e.getRawSlot() >= topInv.getSize() && e.isShiftClick() && e.getCurrentItem() != null && !e.getCurrentItem().getType().isAir()) {
            e.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("chest-no-place"));
            return;
        }

        if (e.getAction().name().contains("PLACE") && e.getRawSlot() < topInv.getSize()) {
            e.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("chest-no-place"));
            return;
        }

        if (e.getRawSlot() >= topInv.getSize()) return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) return;

        if (!plugin.getConfig().getBoolean("anti-steal.enabled")) {
            return;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long cooldown = plugin.getConfig().getLong("anti-steal.cooldown-ms");

        if (lastTakeTime.containsKey(playerId)) {
            long timeSinceLastTake = currentTime - lastTakeTime.get(playerId);

            if (timeSinceLastTake < cooldown) {
                e.setCancelled(true);

                String warningMessage = plugin.getConfigManager().getMessage("anti-steal-warning");
                player.sendMessage(warningMessage);
                return;
            }
        }

        lastTakeTime.put(playerId, currentTime);
    }
}