package com.benserver.weapons.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class CraftLimitManager {

    private final JavaPlugin plugin;
    private final File dataFile;

    // Maps weapon type ID -> set of player UUIDs who have crafted it
    private final Map<String, Set<UUID>> craftedBy = new HashMap<>();

    public CraftLimitManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "crafted.yml");
        load();
    }

    public boolean hasCrafted(UUID player, String weaponType) {
        Set<UUID> set = craftedBy.get(weaponType);
        return set != null && set.contains(player);
    }

    public void markCrafted(UUID player, String weaponType) {
        craftedBy.computeIfAbsent(weaponType, k -> new HashSet<>()).add(player);
        save();
    }

    // ── Persistence ──────────────────────────────────────────────────

    public void load() {
        plugin.getDataFolder().mkdirs();
        if (!dataFile.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        craftedBy.clear();

        for (String weaponType : config.getKeys(false)) {
            List<String> uuids = config.getStringList(weaponType);
            Set<UUID> set = new HashSet<>();
            for (String uuidStr : uuids) {
                try {
                    set.add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Skipping invalid UUID in crafted.yml: " + uuidStr);
                }
            }
            craftedBy.put(weaponType, set);
        }
    }

    public void save() {
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, Set<UUID>> entry : craftedBy.entrySet()) {
            config.set(
                entry.getKey(),
                entry.getValue().stream().map(UUID::toString).toList()
            );
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save crafted.yml", e);
        }
    }
}
