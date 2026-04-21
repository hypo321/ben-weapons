package com.benserver.weapons.listeners;

import com.benserver.weapons.items.CustomWeapons;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final CustomWeapons customWeapons;

    // Inventory types that custom weapons are NOT allowed into
    // (anvils are excluded from this list — they stay allowed)
    private static final java.util.Set<InventoryType> BLOCKED_TYPES = java.util.Set.of(
        InventoryType.CHEST,
        InventoryType.BARREL,
        InventoryType.SHULKER_BOX,
        InventoryType.HOPPER,
        InventoryType.DROPPER,
        InventoryType.DISPENSER,
        InventoryType.ENDER_CHEST,
        InventoryType.FURNACE,
        InventoryType.BLAST_FURNACE,
        InventoryType.SMOKER
    );

    public InventoryListener(CustomWeapons customWeapons) {
        this.customWeapons = customWeapons;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryType topType = event.getView().getTopInventory().getType();
        if (!BLOCKED_TYPES.contains(topType)) return;

        // Check both the cursor item (being placed) and the clicked slot item
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        boolean cursorIsWeapon  = customWeapons.getWeaponType(cursor)  != null;
        boolean currentIsWeapon = customWeapons.getWeaponType(current) != null;

        if (cursorIsWeapon || currentIsWeapon) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Custom weapons cannot be stored in containers.");
        }
    }
}
