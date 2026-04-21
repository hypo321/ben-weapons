package com.benserver.weapons.listeners;

import com.benserver.weapons.items.CustomWeapons;
import com.benserver.weapons.managers.CraftLimitManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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

        if (craftLimitManager.hasBeenCrafted(weaponType)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "✖ This weapon has already been crafted on the server — only one can ever exist.");
            return;
        }

        craftLimitManager.markCrafted(weaponType);
        player.sendMessage(ChatColor.GOLD + "✦ Weapon crafted! You are the only one on the server who will ever have this.");
        broadcastForge(player, weaponType);
    }

    private void broadcastForge(Player crafter, String weaponType) {
        String title;
        String subtitle = ChatColor.WHITE + "There can only be one.";
        Sound sound;

        switch (weaponType) {
            case CustomWeapons.FIRE_SWORD_ID -> {
                title = ChatColor.RED + "" + ChatColor.BOLD
                    + "⚔ " + crafter.getName() + " has forged the Fire Blitz Sword!";
                sound = Sound.ENTITY_BLAZE_SHOOT;
            }
            case CustomWeapons.LIGHTNING_AXE_ID -> {
                title = ChatColor.AQUA + "" + ChatColor.BOLD
                    + "⚡ " + crafter.getName() + " has forged the Lightning Axe!";
                sound = Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
            }
            case CustomWeapons.DASH_MACE_ID -> {
                title = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD
                    + "💨 " + crafter.getName() + " has forged the Dash Mace!";
                sound = Sound.ENTITY_ENDER_PEARL_THROW;
            }
            default -> { return; }
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendTitle(title, subtitle, 10, 80, 20);
            online.playSound(online.getLocation(), sound, 1.0f, 1.0f);
        }
    }
}
