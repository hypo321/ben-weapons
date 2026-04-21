package com.benserver.weapons.commands;

import com.benserver.weapons.items.CustomWeapons;
import com.benserver.weapons.managers.CooldownManager;
import com.benserver.weapons.listeners.WeaponAbilityListener;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SwordCommand implements CommandExecutor {

    private final CustomWeapons customWeapons;
    private final CooldownManager cooldownManager;
    private final WeaponAbilityListener abilityListener;

    public SwordCommand(CustomWeapons customWeapons, CooldownManager cooldownManager,
                        WeaponAbilityListener abilityListener) {
        this.customWeapons = customWeapons;
        this.cooldownManager = cooldownManager;
        this.abilityListener = abilityListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Run this command in-game.");
            return true;
        }

        // Validate: /sword ability activate
        if (args.length < 2
                || !args[0].equalsIgnoreCase("ability")
                || !args[1].equalsIgnoreCase("activate")) {
            player.sendMessage(ChatColor.GOLD + "Usage: /sword ability activate");
            return true;
        }

        String weaponType = customWeapons.getWeaponType(player.getInventory().getItemInMainHand());
        if (!CustomWeapons.FIRE_SWORD_ID.equals(weaponType)) {
            player.sendMessage(ChatColor.RED + "You must be holding the Fire Blitz Sword!");
            return true;
        }

        int cooldownLeft = cooldownManager.getCooldownSeconds(player, CustomWeapons.FIRE_SWORD_ID);
        if (cooldownLeft > 0) {
            player.sendMessage(ChatColor.RED + "⏳ Wait " + cooldownLeft + " more second"
                + (cooldownLeft == 1 ? "" : "s") + "!");
            return true;
        }

        abilityListener.activateFireBlitz(player);
        cooldownManager.setCooldown(player, CustomWeapons.FIRE_SWORD_ID);
        return true;
    }
}
