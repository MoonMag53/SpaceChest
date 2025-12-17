package ru.moonmag.managers;

import ru.moonmag.SpaceChest;
import ru.moonmag.models.LootItem;
import ru.moonmag.models.LootTable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private final SpaceChest plugin;
    private Connection connection;
    private final String dbType;

    public DatabaseManager(SpaceChest plugin) {
        this.plugin = plugin;
        this.dbType = plugin.getConfig().getString("db_type").toLowerCase();
    }

    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            if (dbType.equals("mysql")) {
                String host = plugin.getConfig().getString("database.host");
                int port = plugin.getConfig().getInt("database.port");
                String database = plugin.getConfig().getString("database.database");
                String username = plugin.getConfig().getString("database.username");
                String password = plugin.getConfig().getString("database.password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
                connection = DriverManager.getConnection(url, username, password);
            } else {
                String url = "jdbc:sqlite:" + plugin.getDataFolder() + "/database.db";
                connection = DriverManager.getConnection(url);
            }

            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            String lootTablesSQL;
            if (dbType.equals("mysql")) {
                lootTablesSQL = "CREATE TABLE IF NOT EXISTS loot_tables (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "name VARCHAR(255) UNIQUE NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            } else {
                lootTablesSQL = "CREATE TABLE IF NOT EXISTS loot_tables (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT UNIQUE NOT NULL" +
                        ")";
            }
            stmt.execute(lootTablesSQL);

            String lootItemsSQL;
            if (dbType.equals("mysql")) {
                lootItemsSQL = "CREATE TABLE IF NOT EXISTS loot_items (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "table_name VARCHAR(255) NOT NULL," +
                        "item_data BLOB NOT NULL," +
                        "chance DOUBLE NOT NULL," +
                        "position INT NOT NULL," +
                        "FOREIGN KEY (table_name) REFERENCES loot_tables(name) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            } else {
                lootItemsSQL = "CREATE TABLE IF NOT EXISTS loot_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "table_name TEXT NOT NULL," +
                        "item_data BLOB NOT NULL," +
                        "chance REAL NOT NULL," +
                        "position INTEGER NOT NULL," +
                        "FOREIGN KEY (table_name) REFERENCES loot_tables(name) ON DELETE CASCADE" +
                        ")";
            }
            stmt.execute(lootItemsSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
        }
    }

    public void saveLootTable(String tableName, LootTable table) {
        try {
            String checkSQL = "SELECT name FROM loot_tables WHERE name = ?";
            try (PreparedStatement ps = connection.prepareStatement(checkSQL)) {
                ps.setString(1, tableName);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    String insertSQL = "INSERT INTO loot_tables (name) VALUES (?)";
                    try (PreparedStatement insertPs = connection.prepareStatement(insertSQL)) {
                        insertPs.setString(1, tableName);
                        insertPs.executeUpdate();
                    }
                }
            }

            String deleteSQL = "DELETE FROM loot_items WHERE table_name = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteSQL)) {
                ps.setString(1, tableName);
                ps.executeUpdate();
            }

            String insertSQL = "INSERT INTO loot_items (table_name, item_data, chance, position) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insertSQL)) {
                int position = 0;
                for (LootItem item : table.getItems()) {
                    ps.setString(1, tableName);
                    ps.setBytes(2, serializeItemStack(item.getItemStack()));
                    ps.setDouble(3, item.getChance());
                    ps.setInt(4, position++);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, LootTable> loadLootTables() {
        Map<String, LootTable> tables = new HashMap<>();

        try {
            String sql = "SELECT name FROM loot_tables";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String name = rs.getString("name");
                    LootTable table = loadLootTable(name);
                    if (table != null) {
                        tables.put(name, table);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tables;
    }

    public LootTable loadLootTable(String tableName) {
        try {
            LootTable table = new LootTable(tableName);

            String sql = "SELECT item_data, chance FROM loot_items WHERE table_name = ? ORDER BY position";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, tableName);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    byte[] itemData = rs.getBytes("item_data");
                    double chance = rs.getDouble("chance");

                    ItemStack item = deserializeItemStack(itemData);
                    if (item != null) {
                        table.addItem(new LootItem(item, chance));
                    }
                }
            }

            return table;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteLootTable(String tableName) {
        try {
            String sql = "DELETE FROM loot_tables WHERE name = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, tableName);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private byte[] serializeItemStack(ItemStack item) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(bos)) {
            oos.writeObject(item);
            return bos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private ItemStack deserializeItemStack(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             BukkitObjectInputStream ois = new BukkitObjectInputStream(bis)) {
            return (ItemStack) ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void ensureConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
        }
    }
}