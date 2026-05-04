package com.benserver.weapons.listeners;

import com.benserver.weapons.BenWeaponsPlugin;
import com.benserver.weapons.items.CustomWeapons;
import com.benserver.weapons.managers.CooldownManager;
import com.benserver.weapons.managers.TrustManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.*;
import org.bukkit.util.Vector;

public class WeaponAbilityListener implements Listener {

    private final BenWeaponsPlugin plugin;
    private final CooldownManager cooldownManager;
    private final CustomWeapons customWeapons;
    private final TrustManager trustManager;

    public WeaponAbilityListener(BenWeaponsPlugin plugin, CooldownManager cooldownManager,
                                  CustomWeapons customWeapons, TrustManager trustManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
        this.customWeapons = customWeapons;
        this.trustManager = trustManager;
    }

    // ── MACE: F key (swap-hands) ──────────────────────────────────────
    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        String weaponType = customWeapons.getWeaponType(player.getInventory().getItemInMainHand());
        if (!CustomWeapons.DASH_MACE_ID.equals(weaponType)) return;

        event.setCancelled(true);

        int cooldownLeft = cooldownManager.getCooldownSeconds(player, CustomWeapons.DASH_MACE_ID);
        if (cooldownLeft > 0) {
            player.sendMessage(ChatColor.RED + "⏳ Wait " + cooldownLeft + " more second"
                + (cooldownLeft == 1 ? "" : "s") + "!");
            return;
        }

        activateDash(player);
        cooldownManager.setCooldown(player, CustomWeapons.DASH_MACE_ID);
    }

    // ── AXE: auto-triggers on hit when off cooldown ──────────────────
    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        // Cancel all custom-weapon damage to trusted players
        if (event.getEntity() instanceof Player target) {
            String heldType = customWeapons.getWeaponType(player.getInventory().getItemInMainHand());
            if (heldType != null && trustManager.isTrusted(player.getUniqueId(), target.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }

        String weaponType = customWeapons.getWeaponType(player.getInventory().getItemInMainHand());
        if (!CustomWeapons.LIGHTNING_AXE_ID.equals(weaponType)) return;

        if (cooldownManager.getCooldownSeconds(player, CustomWeapons.LIGHTNING_AXE_ID) > 0) return;

        activateLightningStrike(player);
        cooldownManager.setCooldown(player, CustomWeapons.LIGHTNING_AXE_ID);
    }

    // ── SWORD: activated via /sword ability activate (see SwordCommand) ──
    public void activateFireExplosion(Player player) {
        activateFireExplosion(player, null);
    }

    public void activateFireExplosion(Player player, CooldownManager cooldownManager) {
        var targetBlock = player.getTargetBlockExact(30);
        if (targetBlock == null) {
            player.sendMessage(ChatColor.RED + "No target in range!");
            return;
        }

        Location explosionCenter = targetBlock.getLocation().add(0.5, 1, 0.5);
        World world = player.getWorld();

        // Check for trusted players in the area
        for (Entity nearby : world.getNearbyEntities(explosionCenter, 12, 8, 12)) {
            if (nearby instanceof Player hit && trustManager.isTrusted(player.getUniqueId(), hit.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "Fire explosion redirected — " + hit.getName() + " is nearby.");
                return;
            }
        }

        // Create massive fire explosion
        player.sendMessage(ChatColor.RED + "🔥 FIRE EXPLOSION! " + ChatColor.GRAY + "(45s cooldown)");

        // Visual and sound effects
        world.playSound(explosionCenter, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.8f);
        world.playSound(explosionCenter, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.6f);

        // Create explosion
        world.createExplosion(explosionCenter, 4.0f, false, true); // 4.0 power, no fire break, sets fires

        // Additional fire particles for dramatic effect
        for (int i = 0; i < 50; i++) {
            double offsetX = (Math.random() - 0.5) * 8;
            double offsetY = Math.random() * 4;
            double offsetZ = (Math.random() - 0.5) * 8;
            Location particleLoc = explosionCenter.clone().add(offsetX, offsetY, offsetZ);
            world.spawnParticle(Particle.FLAME, particleLoc, 5, 0.2, 0.2, 0.2, 0.1);
            world.spawnParticle(Particle.LAVA, particleLoc, 2, 0.1, 0.1, 0.1, 0.05);
        }

        // Damage all entities in radius (except trusted players)
        for (Entity entity : world.getNearbyEntities(explosionCenter, 8, 6, 8)) {
            if (entity instanceof LivingEntity living) {
                if (entity instanceof Player target && trustManager.isTrusted(player.getUniqueId(), target.getUniqueId())) {
                    continue; // Skip trusted players
                }

                // Calculate damage based on distance from center
                double distance = living.getLocation().distance(explosionCenter);
                double damage = Math.max(0, 15.0 - (distance * 1.5)); // 15 damage at center, falloff

                if (damage > 0) {
                    living.damage(damage, player);
                    living.setFireTicks(100); // Set on fire for 5 seconds
                }
            }
        }

        // Give player temporary fire resistance
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0, false, true)); // 10 seconds

        // Set cooldown only after successful activation
        if (cooldownManager != null) {
            cooldownManager.setCooldown(player, CustomWeapons.FIRE_SWORD_ID + "_explosion");
        }
    }

    private void activateLightningStrike(Player player) {
        var targetBlock = player.getTargetBlockExact(30);
        Location strike = (targetBlock != null)
            ? targetBlock.getLocation().add(0.5, 0, 0.5)
            : player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(30));

        // Don't lightning-strike trusted players standing at the target location
        for (org.bukkit.entity.Entity nearby : player.getWorld().getNearbyEntities(strike, 2, 2, 2)) {
            if (nearby instanceof Player hit && trustManager.isTrusted(player.getUniqueId(), hit.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "Lightning redirected — " + hit.getName() + " is trusted.");
                return;
            }
        }

        player.getWorld().strikeLightning(strike);

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1, false, true));

        player.sendMessage(ChatColor.AQUA + "⚡ Lightning Strike! "
            + ChatColor.GREEN + "Regen II for 20s. "
            + ChatColor.GRAY + "(25s cooldown)");
    }

    public void activateLightningStorm(Player player) {
        activateLightningStorm(player, null);
    }

    public void activateLightningStorm(Player player, CooldownManager cooldownManager) {
        var targetBlock = player.getTargetBlockExact(40);
        if (targetBlock == null) {
            player.sendMessage(ChatColor.RED + "No target in range!");
            return;
        }

        Location stormCenter = targetBlock.getLocation().add(0.5, 1, 0.5);
        World world = player.getWorld();

        // Check for trusted players in the area
        for (Entity nearby : world.getNearbyEntities(stormCenter, 15, 10, 15)) {
            if (nearby instanceof Player hit && trustManager.isTrusted(player.getUniqueId(), hit.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "Lightning storm redirected — " + hit.getName() + " is nearby.");
                return;
            }
        }

        // Create lightning storm effect
        player.sendMessage(ChatColor.LIGHT_PURPLE + "⚡ LIGHTNING STORM! " + ChatColor.GRAY + "(60s cooldown)");
        world.playSound(stormCenter, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.8f);

        // Schedule multiple lightning strikes
        for (int i = 0; i < 7; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Random position within 10-block radius
                double angle = Math.random() * 2 * Math.PI;
                double radius = Math.random() * 10;
                double x = stormCenter.getX() + Math.cos(angle) * radius;
                double z = stormCenter.getZ() + Math.sin(angle) * radius;
                double y = stormCenter.getY() + Math.random() * 5;

                Location strikeLoc = new Location(world, x, y, z);
                world.strikeLightning(strikeLoc);
                world.spawnParticle(Particle.ELECTRIC_SPARK, strikeLoc, 10, 0.5, 0.5, 0.5, 0.1);
            }, i * 5L); // Strike every 5 ticks (0.25 seconds)
        }

        // Give player regeneration bonus
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 2, false, true)); // Regen III for 30s

        // Set cooldown only after successful activation
        if (cooldownManager != null) {
            cooldownManager.setCooldown(player, CustomWeapons.LIGHTNING_AXE_ID + "_storm");
        }
    }

    private void activateDash(Player player) {
        Vector dir = player.getEyeLocation().getDirection();
        dir.setY(Math.max(dir.getY(), 0.1));
        dir.normalize().multiply(3.0);

        player.setVelocity(dir);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.3, 0.1, 0.3, 0.05);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "💨 Dash! " + ChatColor.GRAY + "(15s cooldown)");
    }
}
