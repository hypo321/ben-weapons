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
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "⚔ Fire Blitz Sword");
        meta.setLore(Arrays.asList(
            ChatColor.GOLD + "Right-click to unleash a blazing fireball!",
            ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + "40 seconds",
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
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "⚡ Lightning Axe");
        meta.setLore(Arrays.asList(
            ChatColor.AQUA + "Right-click to call down a lightning strike!",
            ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + "40 seconds",
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
            ChatColor.LIGHT_PURPLE + "Right-click to dash 20 blocks forward!",
            ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + "30 seconds",
            "",
            ChatColor.YELLOW + "✦ Passive: " + ChatColor.WHITE + "Speed II (always active)"
        ));

        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, DASH_MACE_ID);

        item.setItemMeta(meta);
        return item;
    }

    public void registerRecipes() {
        ShapedRecipe fireSword = new ShapedRecipe(
            new NamespacedKey(plugin, "fire_blitz_sword"), createFireBlitzSword());
        fireSword.shape(" B ", "FSF", " B ");
        fireSword.setIngredient('B', Material.BLAZE_ROD);
        fireSword.setIngredient('F', Material.FIRE_CHARGE);
        fireSword.setIngredient('S', Material.IRON_SWORD);
        Bukkit.addRecipe(fireSword);

        ShapedRecipe lightningAxe = new ShapedRecipe(
            new NamespacedKey(plugin, "lightning_axe"), createLightningAxe());
        lightningAxe.shape("GG ", "GA ", " B ");
        lightningAxe.setIngredient('G', Material.GOLD_INGOT);
        lightningAxe.setIngredient('A', Material.IRON_AXE);
        lightningAxe.setIngredient('B', Material.BLAZE_ROD);
        Bukkit.addRecipe(lightningAxe);

        ShapedRecipe dashMace = new ShapedRecipe(
            new NamespacedKey(plugin, "dash_mace"), createDashMace());
        dashMace.shape("E E", " M ", "E E");
        dashMace.setIngredient('E', Material.ENDER_PEARL);
        dashMace.setIngredient('M', Material.MACE);
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
