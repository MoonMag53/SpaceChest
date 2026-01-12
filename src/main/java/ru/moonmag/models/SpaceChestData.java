package ru.moonmag.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public class SpaceChestData {
    private final String id;
    private final Location location;
    private final Material blockType;
    private final int reloadInterval;
    private final String lootTable;
    private final BlockFace facing;
    private int timeLeft;

    public SpaceChestData(String id, Location location, Material blockType, int reloadInterval, String lootTable, BlockFace facing) {
        this.id = id;
        this.location = location;
        this.blockType = blockType;
        this.reloadInterval = reloadInterval;
        this.lootTable = lootTable;
        this.facing = facing;
        this.timeLeft = reloadInterval;
    }

    public String getId() { return id; }
    public Location getLocation() { return location; }
    public Material getBlockType() { return blockType; }
    public int getReloadInterval() { return reloadInterval; }
    public String getLootTable() { return lootTable; }
    public BlockFace getFacing() { return facing; }
    public int getTimeLeft() { return timeLeft; }

    public void decrementTimer() {
        if (timeLeft > 0) timeLeft--;
    }

    public void resetTimer() {
        timeLeft = reloadInterval;
    }
}
