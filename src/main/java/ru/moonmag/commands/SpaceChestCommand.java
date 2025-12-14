package ru.moonmag.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.moonmag.SpaceChest;
import ru.moonmag.gui.LootGUI;
import ru.moonmag.models.LootTable;
import ru.moonmag.models.SpaceChestData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpaceChestCommand implements CommandExecutor, TabCompleter {
    private final SpaceChest plugin;

    public SpaceChestCommand(SpaceChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("spacechest.use")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getConfigManager().getMessage("command-usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getChestManager().shutdown();
            plugin.getHologramManager().removeAllHolograms();

            plugin.getConfigManager().reload();
            plugin.getLootManager().loadLootTables();
            plugin.getChestManager().loadChests();

            sender.sendMessage(plugin.getConfigManager().getMessage("reload-success"));
            return true;
        }

        if (args[0].equalsIgnoreCase("loot")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(plugin.getConfigManager().getMessage("gui-usage"));
                return true;
            }

            Player player = (Player) sender;
            String chestId = args[1];
            SpaceChestData chestData = plugin.getChestManager().getChests().get(chestId);

            if (chestData == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("gui-usage"));
                return true;
            }

            String lootTableName = chestData.getLootTable();
            LootTable table = plugin.getLootManager().getLootTable(lootTableName);

            if (table == null) {
                table = new LootTable(lootTableName);
                plugin.getLootManager().saveLootTable(lootTableName, table);
            }

            new LootGUI(plugin, player, table).open();
            return true;
        }

        sender.sendMessage(plugin.getConfigManager().getMessage("command-usage"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("spacechest.use")) {
            return completions;
        }

        if (args.length == 1) {
            completions.add("reload");
            completions.add("loot");

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("loot")) {
            completions.addAll(plugin.getChestManager().getChests().keySet());

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return completions;
    }
}