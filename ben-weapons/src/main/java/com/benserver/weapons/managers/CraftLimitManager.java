package com.benserver.weapons.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class CraftLimitManager {

    private final JavaPlugin plugin;
    private final File dataFile;

    // Weapon type IDs that have already been crafted server-wide
    private final Set<String> craftedWeapons = new HashSet<>();

    public CraftLimitManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "crafted.yml");
        load();
    }

    public boolean hasBeenCrafted(String weaponType) {
        return craftedWeapons.contains(weaponType);
    }

    public void markCrafted(String weaponType) {
        craftedWeapons.add(weaponType);
        save();
    }

    // ── Persistence ──────────────────────────────────────────────────

    public void load() {
        plugin.getDataFolder().mkdirs();
        if (!dataFile.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        craftedWeapons.clear();
        craftedWeapons.addAll(config.getStringList("crafted"));
    }

    public void save() {
        FileConfiguration config = new YamlConfiguration();
        config.set("crafted", craftedWeapons.stream().toList());
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save crafted.yml", e);
        }
    }
}
