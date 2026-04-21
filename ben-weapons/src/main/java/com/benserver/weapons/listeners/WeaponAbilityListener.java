package com.benserver.weapons.listeners;

import com.benserver.weapons.BenWeaponsPlugin;
import com.benserver.weapons.items.CustomWeapons;
import com.benserver.weapons.managers.CooldownManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.*;
import org.bukkit.util.Vector;

public class WeaponAbilityListener implements Listener {

    private final BenWeaponsPlugin plugin;
    private final CooldownManager cooldownManager;
    private final CustomWeapons customWeapons;

    public WeaponAbilityListener(BenWeaponsPlugin plugin, CooldownManager cooldownManager,
                                  CustomWeapons customWeapons) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
        this.customWeapons = customWeapons;
    }

    // ── MACE: off-hand key (swap-hands action) ───────────────────────
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (event.getHand() != EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        String weaponType = customWeapons.getWeaponType(player.getInventory().getItemInMainHand());
        if (!CustomWeapons.DASH_MACE_ID.equals(weaponType)) return;

        int cooldownLeft = cooldownManager.getCooldownSeconds(player, CustomWeapons.DASH_MACE_ID);
        if (cooldownLeft > 0) {
            player.sendMessage(ChatColor.RED + "⏳ Wait " + cooldownLeft + " more second"
                + (cooldownLeft == 1 ? "" : "s") + "!");
            return;
        }

        event.setCancelled(true);
        activateDash(player);
        cooldownManager.setCooldown(player, CustomWeapons.DASH_MACE_ID);
    }

    // ── AXE: auto-triggers on hit when off cooldown ──────────────────
    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        String weaponType = customWeapons.getWeaponType(player.getInventory().getItemInMainHand());
        if (!CustomWeapons.LIGHTNING_AXE_ID.equals(weaponType)) return;

        if (cooldownManager.getCooldownSeconds(player, CustomWeapons.LIGHTNING_AXE_ID) > 0) return;

        activateLightningStrike(player);
        cooldownManager.setCooldown(player, CustomWeapons.LIGHTNING_AXE_ID);
    }

    // ── SWORD: activated via /sword ability activate (see SwordCommand) ──
    public void activateFireBlitz(Player player) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();

        Fireball fb = player.getWorld().spawn(eye.add(dir.clone().multiply(1.5)), Fireball.class);
        fb.setDirection(dir.multiply(2));
        fb.setShooter(player);
        fb.setYield(2.5f);
        fb.setIsIncendiary(true);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.FLAME, eye, 20, 0.2, 0.2, 0.2, 0.05);
        player.sendMessage(ChatColor.RED + "🔥 Fire Blitz! " + ChatColor.GRAY + "(30s cooldown)");
    }

    private void activateLightningStrike(Player player) {
        var target = player.getTargetBlockExact(30);
        Location strike = (target != null)
            ? target.getLocation().add(0.5, 0, 0.5)
            : player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(30));

        player.getWorld().strikeLightning(strike);

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1, false, true));

        player.sendMessage(ChatColor.AQUA + "⚡ Lightning Strike! "
            + ChatColor.GREEN + "Regen II for 20s. "
            + ChatColor.GRAY + "(25s cooldown)");
    }

    private void activateDash(Player player) {
        Vector dir = player.getEyeLocation().getDirection();
        dir.setY(Math.max(dir.getY(), 0.1));
        dir.normalize().multiply(1.8);

        player.setVelocity(dir);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.3, 0.1, 0.3, 0.05);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "💨 Dash! " + ChatColor.GRAY + "(15s cooldown)");
    }
}
