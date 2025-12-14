package ru.moonmag.models;

import org.bukkit.inventory.ItemStack;

public class LootItem {

    private final ItemStack itemStack;
    private double chance;

    public LootItem(ItemStack itemStack, double chance) {
        this.itemStack = itemStack.clone();
        this.chance = Math.max(0, Math.min(100, chance));
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = Math.max(0, Math.min(100, chance));
    }
}
