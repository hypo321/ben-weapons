package com.benserver.weapons;

import com.benserver.weapons.commands.SwordCommand;
import com.benserver.weapons.commands.TrustCommand;
import com.benserver.weapons.commands.WeaponsCommand;
import com.benserver.weapons.items.CustomWeapons;
import com.benserver.weapons.listeners.InventoryListener;
import com.benserver.weapons.listeners.WeaponAbilityListener;
import com.benserver.weapons.managers.CooldownManager;
import com.benserver.weapons.managers.TrustManager;
import com.benserver.weapons.tasks.PassiveEffectsTask;
import org.bukkit.plugin.java.JavaPlugin;

public class BenWeaponsPlugin extends JavaPlugin {

    private CooldownManager cooldownManager;
    private CustomWeapons customWeapons;
    private TrustManager trustManager;

    @Override
    public void onEnable() {
        cooldownManager = new CooldownManager();
        customWeapons = new CustomWeapons(this);
        trustManager = new TrustManager(this);

        customWeapons.registerRecipes();

        WeaponAbilityListener abilityListener = new WeaponAbilityListener(this, cooldownManager, customWeapons, trustManager);
        getServer().getPluginManager().registerEvents(abilityListener, this);
        getServer().getPluginManager().registerEvents(new InventoryListener(customWeapons), this);

        new PassiveEffectsTask(customWeapons).runTaskTimer(this, 0L, 20L);

        getCommand("benweapons").setExecutor(new WeaponsCommand(customWeapons));
        getCommand("benweapons-sword").setExecutor(new SwordCommand(customWeapons, cooldownManager, abilityListener));
        TrustCommand trustCmd   = new TrustCommand(trustManager, true);
        TrustCommand untrustCmd = new TrustCommand(trustManager, false);
        getCommand("benweapons-trust").setExecutor(trustCmd);
        getCommand("benweapons-trust").setTabCompleter(trustCmd);
        getCommand("benweapons-untrust").setExecutor(untrustCmd);
        getCommand("benweapons-untrust").setTabCompleter(untrustCmd);

        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("║   Ben's Custom Weapons — Loaded!     ║");
        getLogger().info("║   Fire Blitz Sword  ✓                ║");
        getLogger().info("║   Lightning Axe     ✓                ║");
        getLogger().info("║   Dash Mace         ✓                ║");
        getLogger().info("╚══════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        if (trustManager != null) trustManager.save();
        getLogger().info("Ben's Custom Weapons plugin disabled.");
    }

    public CooldownManager getCooldownManager() { return cooldownManager; }
    public CustomWeapons getCustomWeapons() { return customWeapons; }
}
