package ru.moonmag;

import org.bukkit.plugin.java.JavaPlugin;
import ru.moonmag.commands.SpaceChestCommand;
import ru.moonmag.listeners.ChestListener;
import ru.moonmag.managers.ChestManager;
import ru.moonmag.managers.ConfigManager;
import ru.moonmag.managers.HologramManager;
import ru.moonmag.managers.LootManager;
import ru.moonmag.utils.UpdateChecker;
import ru.moonmag.utils.Metrics;

public class SpaceChest extends JavaPlugin {
    private static SpaceChest instance;
    private ConfigManager configManager;
    private ChestManager chestManager;
    private LootManager lootManager;
    private HologramManager hologramManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        lootManager = new LootManager(this);
        chestManager = new ChestManager(this);
        hologramManager = new HologramManager(this);

        configManager.load();
        lootManager.loadLootTables();
        chestManager.loadChests();

        new Metrics(this, 28352);
        new UpdateChecker(this).checkForUpdates();

        SpaceChestCommand command = new SpaceChestCommand(this);
        getCommand("spacechest").setExecutor(command);
        getCommand("spacechest").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new ChestListener(this), this);

        getLogger().info("§x§f§f§7§c§0§0╔");
        getLogger().info("§x§f§f§7§c§0§0║ §fЗапуск плагина...");
        getLogger().info("§x§f§f§7§c§0§0║ §x§0§0§f§f§1§7Плагин запустился! §fКодер: §x§f§f§6§e§0§0SpaceDev");
        getLogger().info("§x§f§f§7§c§0§0║ §x§0§0§f§5§f§fh§x§0§0§f§4§f§ft§x§0§0§f§3§f§ft§x§0§0§f§2§f§fp§x§0§0§f§1§f§fs§x§0§0§e§f§f§f:§x§0§0§e§e§f§f/§x§0§0§e§d§f§f/§x§0§0§e§c§f§ft§x§0§0§e§b§f§f.§x§0§0§e§a§f§fm§x§0§0§e§9§f§fe§x§0§0§e§8§f§f/§x§0§0§e§7§f§fs§x§0§0§e§5§f§fp§x§0§0§e§4§f§fa§x§0§0§e§3§f§fc§x§0§0§e§2§f§fe§x§0§0§e§1§f§fs§x§0§0§e§0§f§ft§x§0§0§d§f§f§fu§x§0§0§d§e§f§fd§x§0§0§d§c§f§fi§x§0§0§d§b§f§fo§x§0§0§d§a§f§fm§x§0§0§d§9§f§fc");
        getLogger().info("§x§f§f§7§c§0§0╚");
    }

    @Override
    public void onDisable() {
        if (chestManager != null) {
            chestManager.shutdown();
        }
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }

        getLogger().info("§x§f§f§7§c§0§0╔");
        getLogger().info("§x§f§f§7§c§0§0║ §fОтключение плагина...");
        getLogger().info("§x§f§f§7§c§0§0║ §x§f§f§0§0§0§0Плагин отключился! §fКодер: §x§f§f§6§e§0§0SpaceDev");
        getLogger().info("§x§f§f§7§c§0§0║ §x§0§0§f§5§f§fh§x§0§0§f§4§f§ft§x§0§0§f§3§f§ft§x§0§0§f§2§f§fp§x§0§0§f§1§f§fs§x§0§0§e§f§f§f:§x§0§0§e§e§f§f/§x§0§0§e§d§f§f/§x§0§0§e§c§f§ft§x§0§0§e§b§f§f.§x§0§0§e§a§f§fm§x§0§0§e§9§f§fe§x§0§0§e§8§f§f/§x§0§0§e§7§f§fs§x§0§0§e§5§f§fp§x§0§0§e§4§f§fa§x§0§0§e§3§f§fc§x§0§0§e§2§f§fe§x§0§0§e§1§f§fs§x§0§0§e§0§f§ft§x§0§0§d§f§f§fu§x§0§0§d§e§f§fd§x§0§0§d§c§f§fi§x§0§0§d§b§f§fo§x§0§0§d§a§f§fm§x§0§0§d§9§f§fc");
        getLogger().info("§x§f§f§7§c§0§0╚");
    }

    public static SpaceChest getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }
}