package com.benserver.weapons;

import com.benserver.weapons.commands.SwordCommand;
import com.benserver.weapons.commands.TrustCommand;
import com.benserver.weapons.commands.WeaponsCommand;
import com.benserver.weapons.items.CustomWeapons;
import com.benserver.weapons.listeners.CraftLimitListener;
import com.benserver.weapons.listeners.InventoryListener;
import com.benserver.weapons.listeners.WeaponAbilityListener;
import com.benserver.weapons.managers.CooldownManager;
import com.benserver.weapons.managers.CraftLimitManager;
import com.benserver.weapons.managers.TrustManager;
import com.benserver.weapons.tasks.PassiveEffectsTask;
import org.bukkit.plugin.java.JavaPlugin;

public class BenWeaponsPlugin extends JavaPlugin {

    private CooldownManager cooldownManager;
    private CustomWeapons customWeapons;
    private TrustManager trustManager;
    private CraftLimitManager craftLimitManager;

    @Override
    public void onEnable() {
        cooldownManager = new CooldownManager();
        customWeapons = new CustomWeapons(this);
        trustManager = new TrustManager(this);
        craftLimitManager = new CraftLimitManager(this);

        customWeapons.registerRecipes();

        WeaponAbilityListener abilityListener = new WeaponAbilityListener(this, cooldownManager, customWeapons, trustManager);
        getServer().getPluginManager().registerEvents(abilityListener, this);
        getServer().getPluginManager().registerEvents(new InventoryListener(customWeapons), this);
        getServer().getPluginManager().registerEvents(new CraftLimitListener(customWeapons, craftLimitManager), this);

        new PassiveEffectsTask(customWeapons).runTaskTimer(this, 0L, 20L);

        registerResourcePackListener();

        getCommand("benweapons").setExecutor(new WeaponsCommand(customWeapons, this));
        getCommand("sword").setExecutor(new SwordCommand(customWeapons, cooldownManager, abilityListener));
        TrustCommand trustCmd   = new TrustCommand(trustManager, true);
        TrustCommand untrustCmd = new TrustCommand(trustManager, false);
        getCommand("trust").setExecutor(trustCmd);
        getCommand("trust").setTabCompleter(trustCmd);
        getCommand("untrust").setExecutor(untrustCmd);
        getCommand("untrust").setTabCompleter(untrustCmd);

        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("║   Ben's Custom Weapons — Loaded!     ║");
        getLogger().info("║   Forbidden Sword   ✓                ║");
        getLogger().info("║   Forbidden Axe     ✓                ║");
        getLogger().info("║   Forbidden Mace    ✓                ║");
        getLogger().info("╚══════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        if (trustManager != null) trustManager.save();
        if (craftLimitManager != null) craftLimitManager.save();
        getLogger().info("Ben's Custom Weapons plugin disabled.");
    }

    public CooldownManager getCooldownManager() { return cooldownManager; }
    public CustomWeapons getCustomWeapons() { return customWeapons; }

    private void registerResourcePackListener() {
        String sha1;
        try (var in = getResource("plugin.yml")) {
            if (in == null) return;
            org.bukkit.configuration.file.YamlConfiguration yml =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(in));
            sha1 = yml.getString("resource-pack-sha1", "");
        } catch (Exception e) {
            getLogger().warning("Could not read resource-pack-sha1: " + e.getMessage());
            return;
        }

        if (sha1.isEmpty() || sha1.startsWith("${")) {
            getLogger().info("Resource pack SHA-1 not embedded — skipping auto resource pack.");
            return;
        }

        final String finalSha1 = sha1;
        final String url = "https://github.com/hypo321/ben-weapons/releases/latest/download/BenWeapons-ResourcePack.zip";
        final net.kyori.adventure.text.Component prompt =
            net.kyori.adventure.text.Component.text("Install Ben's Forbidden Weapons textures!");

        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
                event.getPlayer().setResourcePack(url, finalSha1, false, prompt);
            }
        }, this);

        getLogger().info("Resource pack auto-send registered (SHA-1: " + finalSha1.substring(0, 8) + "...)");
    }
}
