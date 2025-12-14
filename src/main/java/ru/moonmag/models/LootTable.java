package ru.moonmag.models;

import java.util.ArrayList;
import java.util.List;

public class LootTable {
    private final String name;
    private final List<LootItem> items;

    public LootTable(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.items = new ArrayList<>();
    }

    public void addItem(LootItem item) {
        items.add(item);
    }

    public void removeItem(LootItem item) {
        items.remove(item);
    }

    public List<LootItem> getItems() {
        return items;
    }

    public String getName() {
        return name;
    }
}