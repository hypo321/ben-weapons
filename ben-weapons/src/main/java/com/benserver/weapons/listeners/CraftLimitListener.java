package com.benserver.weapons.listeners;

import com.benserver.weapons.items.CustomWeapons;
import com.benserver.weapons.managers.CraftLimitManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftLimitListener implements Listener {

    private final CustomWeapons customWeapons;
    private final CraftLimitManager craftLimitManager;

    public CraftLimitListener(CustomWeapons customWeapons, CraftLimitManager craftLimitManager) {
        this.customWeapons = customWeapons;
        this.craftLimitManager = craftLimitManager;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack result = event.getRecipe().getResult();
        String weaponType = customWeapons.getWeaponType(result);
        if (weaponType == null) return;

        if (craftLimitManager.hasCrafted(player.getUniqueId(), weaponType)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "✖ You have already crafted this weapon — each player can only craft it once.");
            return;
        }

        craftLimitManager.markCrafted(player.getUniqueId(), weaponType);
        player.sendMessage(ChatColor.GOLD + "✦ Weapon crafted! This is a one-time craft — keep it safe.");
    }
}
