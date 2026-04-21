package com.benserver.weapons.commands;

import com.benserver.weapons.items.CustomWeapons;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class WeaponsCommand implements CommandExecutor {

    private final CustomWeapons customWeapons;
    private final JavaPlugin plugin;

    public WeaponsCommand(CustomWeapons customWeapons, JavaPlugin plugin) {
        this.customWeapons = customWeapons;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /benweapons version — available to everyone, works from console too
        if (args.length == 1 && args[0].equalsIgnoreCase("version")) {
            String version = plugin.getDescription().getVersion();
            sender.sendMessage(ChatColor.GOLD + "BenWeapons" + ChatColor.GRAY + " v" + ChatColor.WHITE + version);
            return true;
        }

        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage("Run this command in-game.");
            return true;
        }
        if (!senderPlayer.hasPermission("benweapons.give")) {
            senderPlayer.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        if (args.length == 0) {
            senderPlayer.sendMessage(ChatColor.GOLD + "Usage: /benweapons <sword|axe|mace|version> [player]");
            return true;
        }

        ItemStack weapon = switch (args[0].toLowerCase()) {
            case "sword" -> customWeapons.createFireBlitzSword();
            case "axe"   -> customWeapons.createLightningAxe();
            case "mace"  -> customWeapons.createDashMace();
            default      -> null;
        };

        if (weapon == null) {
            senderPlayer.sendMessage(ChatColor.RED + "Unknown weapon! Use: sword, axe, or mace");
            return true;
        }

        Player target = (args.length >= 2) ? Bukkit.getPlayer(args[1]) : senderPlayer;
        if (target == null) {
            senderPlayer.sendMessage(ChatColor.RED + "Player not found or offline.");
            return true;
        }

        target.getInventory().addItem(weapon);
        target.sendMessage(ChatColor.GOLD + "✦ You received: " + weapon.getItemMeta().getDisplayName());
        if (!target.equals(senderPlayer)) {
            senderPlayer.sendMessage(ChatColor.GREEN + "Gave weapon to " + target.getName() + ".");
        }

        return true;
    }
}
