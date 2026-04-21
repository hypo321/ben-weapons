package com.benserver.weapons.items;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class CustomWeapons {

    public static final String FIRE_SWORD_ID    = "fire_blitz_sword";
    public static final String LIGHTNING_AXE_ID = "lightning_axe";
    public static final String DASH_MACE_ID     = "dash_mace";

    private final NamespacedKey weaponKey;
    private final JavaPlugin plugin;

    public CustomWeapons(JavaPlugin plugin) {
        this.plugin = plugin;
        this.weaponKey = new NamespacedKey(plugin, "weapon_type");
    }

    public ItemStack createFireBlitzSword() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "⚔ Fire Blitz Sword");
        meta.setLore(Arrays.asList(
            ChatColor.GOLD + "Use /sword ability activate to unleash a fireball!",
            ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + "30 seconds",
            "",
            ChatColor.YELLOW + "✦ Passive: " + ChatColor.WHITE + "Fire Resistance (always active)"
        ));

        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, FIRE_SWORD_ID);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createLightningAxe() {
        ItemStack item = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "⚡ Lightning Axe");
        meta.setLore(Arrays.asList(
            ChatColor.AQUA + "Auto-strikes lightning on hit when off cooldown!",
            ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + "25 seconds",
            "",
            ChatColor.GREEN + "✦ On Use: " + ChatColor.WHITE + "Regeneration II (20 seconds)"
        ));

        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, LIGHTNING_AXE_ID);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createDashMace() {
        ItemStack item = new ItemStack(Material.MACE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "💨 Dash Mace");
        meta.setLore(Arrays.asList(
            ChatColor.LIGHT_PURPLE + "Press F (off-hand) to dash 20 blocks forward!",
            ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + "15 seconds",
            "",
            ChatColor.YELLOW + "✦ Passive: " + ChatColor.WHITE + "Speed II (always active)"
        ));

        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addEnchant(Enchantment.DENSITY, 5, true);
        meta.addEnchant(Enchantment.BREACH, 4, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, DASH_MACE_ID);

        item.setItemMeta(meta);
        return item;
    }

    // ── WEAPON 1: Fire Blitz Sword ──────────────────────────────────
    // Recipe:  [H] [I] [H]
    //          [B] [Q] [B]
    //          [H] [R] [H]
    // H=Player Head, I=Netherite Ingot, B=Blaze Powder, Q=Nether Quartz, R=Blaze Rod

    // ── WEAPON 2: Lightning Axe ─────────────────────────────────────
    // Recipe:  [H] [A] [H]
    //          [G] [L] [G]
    //          [H] [L] [H]
    // H=Player Head, A=Netherite Axe, G=Gold Ingot, L=Lightning Rod

    // ── WEAPON 3: Dash Mace ─────────────────────────────────────────
    // Recipe:  [H] [C] [H]
    //          [E] [I] [E]
    //          [H] [B] [H]
    // H=Player Head, C=Heavy Core, E=Echo Shard, I=Netherite Ingot, B=Breeze Rod

    public void registerRecipes() {
        ShapedRecipe fireSword = new ShapedRecipe(
            new NamespacedKey(plugin, "fire_blitz_sword"), createFireBlitzSword());
        fireSword.shape("HIH", "BQB", "HRH");
        fireSword.setIngredient('H', Material.PLAYER_HEAD);
        fireSword.setIngredient('I', Material.NETHERITE_INGOT);
        fireSword.setIngredient('B', Material.BLAZE_POWDER);
        fireSword.setIngredient('Q', Material.QUARTZ);
        fireSword.setIngredient('R', Material.BLAZE_ROD);
        Bukkit.addRecipe(fireSword);

        ShapedRecipe lightningAxe = new ShapedRecipe(
            new NamespacedKey(plugin, "lightning_axe"), createLightningAxe());
        lightningAxe.shape("HAH", "GLG", "HLH");
        lightningAxe.setIngredient('H', Material.PLAYER_HEAD);
        lightningAxe.setIngredient('A', Material.NETHERITE_AXE);
        lightningAxe.setIngredient('G', Material.GOLD_INGOT);
        lightningAxe.setIngredient('L', Material.LIGHTNING_ROD);
        Bukkit.addRecipe(lightningAxe);

        ShapedRecipe dashMace = new ShapedRecipe(
            new NamespacedKey(plugin, "dash_mace"), createDashMace());
        dashMace.shape("HCH", "EIE", "HBH");
        dashMace.setIngredient('H', Material.PLAYER_HEAD);
        dashMace.setIngredient('C', Material.HEAVY_CORE);
        dashMace.setIngredient('E', Material.ECHO_SHARD);
        dashMace.setIngredient('I', Material.NETHERITE_INGOT);
        dashMace.setIngredient('B', Material.BREEZE_ROD);
        Bukkit.addRecipe(dashMace);

        plugin.getLogger().info("Custom weapon recipes registered!");
    }

    public void applyPassiveEffects(Player player) {
        boolean hasSword = false;
        boolean hasMace  = false;

        for (ItemStack item : player.getInventory().getContents()) {
            String type = getWeaponType(item);
            if (FIRE_SWORD_ID.equals(type)) hasSword = true;
            if (DASH_MACE_ID.equals(type))  hasMace  = true;
        }

        if (hasSword) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE, 60, 0, false, false));
        }

        if (hasMace) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, 60, 1, false, false));
        }
    }

    public String getWeaponType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(weaponKey, PersistentDataType.STRING)) return null;
        return meta.getPersistentDataContainer().get(weaponKey, PersistentDataType.STRING);
    }

    public NamespacedKey getWeaponKey() { return weaponKey; }
    public JavaPlugin getPlugin() { return plugin; }
}
