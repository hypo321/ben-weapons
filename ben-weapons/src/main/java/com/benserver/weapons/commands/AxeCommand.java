package com.benserver.weapons.commands;

import com.benserver.weapons.items.CustomWeapons;
import com.benserver.weapons.listeners.WeaponAbilityListener;
import com.benserver.weapons.managers.CooldownManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AxeCommand implements CommandExecutor {

    private final CustomWeapons customWeapons;
    private final CooldownManager cooldownManager;
    private final WeaponAbilityListener abilityListener;

    public AxeCommand(CustomWeapons customWeapons, CooldownManager cooldownManager, WeaponAbilityListener abilityListener) {
        this.customWeapons = customWeapons;
        this.cooldownManager = cooldownManager;
        this.abilityListener = abilityListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        String weaponType = customWeapons.getWeaponType(weapon);

        if (!CustomWeapons.LIGHTNING_AXE_ID.equals(weaponType)) {
            player.sendMessage(ChatColor.RED + "You must be holding a Forbidden Axe to use this ability!");
            return true;
        }

        // Check cooldown for lightning storm (separate from auto-strike cooldown)
        String stormCooldownKey = CustomWeapons.LIGHTNING_AXE_ID + "_storm";
        int cooldownLeft = cooldownManager.getCooldownSeconds(player, stormCooldownKey);
        if (cooldownLeft > 0) {
            player.sendMessage(ChatColor.RED + "⏳ Lightning storm on cooldown! Wait " + cooldownLeft + " more second"
                + (cooldownLeft == 1 ? "" : "s") + "!");
            return true;
        }

        // Activate lightning storm
        abilityListener.activateLightningStorm(player);
        cooldownManager.setCooldown(player, stormCooldownKey); // Uses hardcoded duration

        return true;
    }
}
