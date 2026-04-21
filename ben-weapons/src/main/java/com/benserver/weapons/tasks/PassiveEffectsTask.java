package com.benserver.weapons.tasks;

import com.benserver.weapons.items.CustomWeapons;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PassiveEffectsTask extends BukkitRunnable {

    private final CustomWeapons customWeapons;

    public PassiveEffectsTask(CustomWeapons customWeapons) {
        this.customWeapons = customWeapons;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            customWeapons.applyPassiveEffects(player);
        }
    }
}
