package com.benserver.weapons.listeners;

import com.benserver.weapons.items.CustomWeapons;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CustomDamageListener implements Listener {

    private final CustomWeapons customWeapons;

    public CustomDamageListener(CustomWeapons customWeapons) {
        this.customWeapons = customWeapons;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Only process player attacks with melee weapons
        if (!(event.getDamager() instanceof Player)) return;
        
        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        
        // Check if this is one of our custom weapons
        String weaponType = customWeapons.getWeaponType(weapon);
        if (weaponType == null) return;
        
        // Get the custom damage bonus from PDC
        Double damageBonus = weapon.getItemMeta().getPersistentDataContainer()
            .get(customWeapons.getDamageBonusKey(), PersistentDataType.DOUBLE);
        
        if (damageBonus != null && damageBonus > 0) {
            // Apply the custom damage bonus
            double originalDamage = event.getDamage();
            event.setDamage(originalDamage + damageBonus);
            
            customWeapons.getPlugin().getLogger().info(String.format(
                "Applied %.1f custom damage bonus with %s (original: %.1f, total: %.1f)",
                damageBonus, weaponType, originalDamage, event.getDamage()
            ));
        }
    }
}
