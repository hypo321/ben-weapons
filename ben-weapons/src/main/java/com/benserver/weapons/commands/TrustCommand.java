package com.benserver.weapons.commands;

import com.benserver.weapons.managers.TrustManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TrustCommand implements CommandExecutor {

    private final TrustManager trustManager;
    private final boolean isTrust;

    /**
     * @param isTrust true = /trust command, false = /untrust command
     */
    public TrustCommand(TrustManager trustManager, boolean isTrust) {
        this.trustManager = trustManager;
        this.isTrust = isTrust;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Run this command in-game.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "Usage: /" + label + " <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player '" + args[0] + "' is not online.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You can't " + label + " yourself.");
            return true;
        }

        if (isTrust) {
            trustManager.trust(player.getUniqueId(), target.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "✔ Trusted " + ChatColor.WHITE + target.getName()
                + ChatColor.GREEN + " — your weapons will no longer harm them.");
            target.sendMessage(ChatColor.GREEN + "✔ " + ChatColor.WHITE + player.getName()
                + ChatColor.GREEN + " has trusted you — their weapons won't harm you.");
        } else {
            trustManager.untrust(player.getUniqueId(), target.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "✖ Untrusted " + ChatColor.WHITE + target.getName()
                + ChatColor.YELLOW + " — your weapons can now harm them again.");
            target.sendMessage(ChatColor.YELLOW + "✖ " + ChatColor.WHITE + player.getName()
                + ChatColor.YELLOW + " has untrusted you.");
        }

        return true;
    }
}
