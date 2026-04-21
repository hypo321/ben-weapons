package com.benserver.weapons.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class TrustManager {

    private final JavaPlugin plugin;
    private final File trustFile;
    private FileConfiguration trustConfig;

    // Maps each player's UUID to the set of UUIDs they have trusted
    private final Map<UUID, Set<UUID>> trustLists = new HashMap<>();

    public TrustManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.trustFile = new File(plugin.getDataFolder(), "trust.yml");
        load();
    }

    public void trust(UUID owner, UUID trusted) {
        trustLists.computeIfAbsent(owner, k -> new HashSet<>()).add(trusted);
        save();
    }

    public void untrust(UUID owner, UUID trusted) {
        Set<UUID> list = trustLists.get(owner);
        if (list != null) list.remove(trusted);
        save();
    }

    public boolean isTrusted(UUID owner, UUID target) {
        Set<UUID> list = trustLists.get(owner);
        return list != null && list.contains(target);
    }

    public Set<UUID> getTrusted(UUID owner) {
        return trustLists.getOrDefault(owner, Collections.emptySet());
    }

    // ── Persistence ──────────────────────────────────────────────────

    public void load() {
        plugin.getDataFolder().mkdirs();
        if (!trustFile.exists()) {
            trustConfig = new YamlConfiguration();
            return;
        }

        trustConfig = YamlConfiguration.loadConfiguration(trustFile);
        trustLists.clear();

        for (String ownerStr : trustConfig.getKeys(false)) {
            List<String> trustedList = trustConfig.getStringList(ownerStr);
            Set<UUID> trusted = new HashSet<>();
            for (String uuidStr : trustedList) {
                try {
                    trusted.add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Skipping invalid UUID in trust.yml: " + uuidStr);
                }
            }
            try {
                trustLists.put(UUID.fromString(ownerStr), trusted);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipping invalid owner UUID in trust.yml: " + ownerStr);
            }
        }

        plugin.getLogger().info("Loaded trust lists for " + trustLists.size() + " player(s).");
    }

    public void save() {
        trustConfig = new YamlConfiguration();

        for (Map.Entry<UUID, Set<UUID>> entry : trustLists.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                trustConfig.set(
                    entry.getKey().toString(),
                    entry.getValue().stream().map(UUID::toString).toList()
                );
            }
        }

        try {
            trustConfig.save(trustFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save trust.yml", e);
        }
    }
}
