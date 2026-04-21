package com.benserver.weapons.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public int getCooldownSeconds(Player player, String weaponId) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;

        Long lastUsed = playerCooldowns.get(weaponId);
        if (lastUsed == null) return 0;

        long cooldownMillis = getCooldownDuration(weaponId) * 1000L;
        long remaining = cooldownMillis - (System.currentTimeMillis() - lastUsed);

        if (remaining <= 0) return 0;
        return (int) Math.ceil(remaining / 1000.0);
    }

    public void setCooldown(Player player, String weaponId) {
        cooldowns
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .put(weaponId, System.currentTimeMillis());
    }

    private int getCooldownDuration(String weaponId) {
        return switch (weaponId) {
            case "fire_blitz_sword" -> 30;
            case "lightning_axe"   -> 25;
            case "dash_mace"       -> 15;
            default                -> 30;
        };
    }

    public void removePlayer(UUID playerId) {
        cooldowns.remove(playerId);
    }
}
