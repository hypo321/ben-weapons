# Ben's Custom Weapons — Claude Code Build Spec

## What This Is

A Paper Minecraft server plugin that adds three craftable custom weapons to Ben's server.
Players can craft them like normal items — they're a perk of being on his server.

## Prerequisites (Claude Code should check these first)

1. **Java 21+** must be installed (`java -version`)
2. **Maven** must be installed (`mvn -version`)  
   — If missing on Mac: `brew install maven`
3. Create the project folder at `~/minecrafting/ben-weapons/`

---

## Task for Claude Code

**Please create the following Maven project, compile it, and place the final `BenWeapons.jar`
in `~/minecrafting/` ready for Paul to drop into a Minecraft server's `plugins/` folder.**

---

## Project Structure to Create

```
ben-weapons/
├── pom.xml
└── src/main/
    ├── resources/
    │   └── plugin.yml
    └── java/com/benserver/weapons/
        ├── BenWeaponsPlugin.java
        ├── commands/
        │   └── WeaponsCommand.java
        ├── items/
        │   └── CustomWeapons.java
        ├── listeners/
        │   └── WeaponAbilityListener.java
        ├── managers/
        │   └── CooldownManager.java
        └── tasks/
            └── PassiveEffectsTask.java
```

---

## File Contents

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.benserver</groupId>
    <artifactId>ben-weapons</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <repositories>
        <repository>
            <id>papermc</id>
            <url>https://repo.papermc.io/repository/maven-public/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>io.papermc.paper</groupId>
            <artifactId>paper-api</artifactId>
            <version>1.21.4-R0.1-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <finalName>BenWeapons</finalName>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### src/main/resources/plugin.yml

```yaml
name: BenWeapons
version: 1.0.0
main: com.benserver.weapons.BenWeaponsPlugin
api-version: '1.21'
description: Custom craftable weapons exclusive to Ben's server!
author: Ben (with a little help from Claude!)

commands:
  benweapons:
    description: Give a custom weapon to a player (admin only)
    usage: /benweapons <sword|axe|mace> [player]
    permission: benweapons.give

permissions:
  benweapons.give:
    description: Allows giving custom weapons to players
    default: op
```

---

### src/main/java/com/benserver/weapons/BenWeaponsPlugin.java

```java
package com.benserver.weapons;

import com.benserver.weapons.commands.WeaponsCommand;
import com.benserver.weapons.items.CustomWeapons;
import com.benserver.weapons.listeners.WeaponAbilityListener;
import com.benserver.weapons.managers.CooldownManager;
import com.benserver.weapons.tasks.PassiveEffectsTask;
import org.bukkit.plugin.java.JavaPlugin;

public class BenWeaponsPlugin extends JavaPlugin {

    private CooldownManager cooldownManager;
    private CustomWeapons customWeapons;

    @Override
    public void onEnable() {
        cooldownManager = new CooldownManager();
        customWeapons = new CustomWeapons(this);

        customWeapons.registerRecipes();

        getServer().getPluginManager().registerEvents(
            new WeaponAbilityListener(this, cooldownManager, customWeapons), this
        );

        new PassiveEffectsTask(customWeapons).runTaskTimer(this, 0L, 20L);

        getCommand("benweapons").setExecutor(new WeaponsCommand(customWeapons));

        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("║   Ben's Custom Weapons — Loaded!     ║");
        getLogger().info("║   Fire Blitz Sword  ✓                ║");
        getLogger().info("║   Lightning Axe     ✓                ║");
        getLogger().info("║   Dash Mace         ✓                ║");
        getLogger().info("╚══════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        getLogger().info("Ben's Custom Weapons plugin disabled.");
    }

    public CooldownManager getCooldownManager() { return cooldownManager; }
    public CustomWeapons getCustomWeapons() { return customWeapons; }
}
```

---

### src/main/java/com/benserver/weapons/items/CustomWeapons.java

```java
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

    // ── WEAPON 1: Fire Blitz Sword ──────────────────────────────────
    // Recipe:  [ _ ] [B] [ _ ]
    //          [F]   [S] [F]
    //          [ _ ] [B] [ _ ]
    // B=Blaze Rod, F=Fire Charge, S=Iron Sword
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

    // ── WEAPON 2: Lightning Axe ─────────────────────────────────────
    // Recipe:  [G] [G] [ ]
    //          [G] [A] [ ]
    //          [ ] [B] [ ]
    // G=Gold Ingot, A=Iron Axe, B=Blaze Rod
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

    // ── WEAPON 3: Dash Mace ─────────────────────────────────────────
    // Recipe:  [E] [ ] [E]
    //          [ ] [M] [ ]
    //          [E] [ ] [E]
    // E=Ender Pearl, M=Mace
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

    // ── REGISTER CRAFTING RECIPES ───────────────────────────────────
    public void registerRecipes() {
        // Fire Blitz Sword
        ShapedRecipe fireSword = new ShapedRecipe(
            new NamespacedKey(plugin, "fire_blitz_sword"), createFireBlitzSword());
        fireSword.shape(" B ", "FSF", " B ");
        fireSword.setIngredient('B', Material.BLAZE_ROD);
        fireSword.setIngredient('F', Material.FIRE_CHARGE);
        fireSword.setIngredient('S', Material.IRON_SWORD);
        Bukkit.addRecipe(fireSword);

        // Lightning Axe
        ShapedRecipe lightningAxe = new ShapedRecipe(
            new NamespacedKey(plugin, "lightning_axe"), createLightningAxe());
        lightningAxe.shape("GG ", "GA ", " B ");
        lightningAxe.setIngredient('G', Material.GOLD_INGOT);
        lightningAxe.setIngredient('A', Material.IRON_AXE);
        lightningAxe.setIngredient('B', Material.BLAZE_ROD);
        Bukkit.addRecipe(lightningAxe);

        // Dash Mace
        ShapedRecipe dashMace = new ShapedRecipe(
            new NamespacedKey(plugin, "dash_mace"), createDashMace());
        dashMace.shape("E E", " M ", "E E");
        dashMace.setIngredient('E', Material.ENDER_PEARL);
        dashMace.setIngredient('M', Material.MACE);
        Bukkit.addRecipe(dashMace);

        plugin.getLogger().info("Custom weapon recipes registered!");
    }

    // ── PASSIVE EFFECTS (called every second) ───────────────────────
    public void applyPassiveEffects(Player player) {
        boolean hasSword = false;
        boolean hasMace  = false;

        for (ItemStack item : player.getInventory().getContents()) {
            String type = getWeaponType(item);
            if (FIRE_SWORD_ID.equals(type)) hasSword = true;
            if (DASH_MACE_ID.equals(type))  hasMace  = true;
        }

        // Fire Resistance — refreshed every second so it never expires
        if (hasSword) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE, 60, 0, false, false));
        }

        // Speed II — refreshed every second so it never expires
        if (hasMace) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, 60, 1, false, false));
        }
    }

    // ── HELPER ──────────────────────────────────────────────────────
    public String getWeaponType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(weaponKey, PersistentDataType.STRING)) return null;
        return meta.getPersistentDataContainer().get(weaponKey, PersistentDataType.STRING);
    }

    public NamespacedKey getWeaponKey() { return weaponKey; }
    public JavaPlugin getPlugin() { return plugin; }
}
```

---

### src/main/java/com/benserver/weapons/managers/CooldownManager.java

```java
package com.benserver.weapons.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public int getCooldownSeconds(Player player, String weaponId) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;

        Long lastUsed = playerCooldowns.get(weaponId);
        if (lastUsed == null) return 0;

        long cooldownMillis = getCooldownDuration(weaponId) * 1000L;
        long remaining = cooldownMillis - (System.currentTimeMillis() - lastUsed);

        if (remaining <= 0) return 0;
        return (int) Math.ceil(remaining / 1000.0);
    }

    public void setCooldown(Player player, String weaponId) {
        cooldowns
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .put(weaponId, System.currentTimeMillis());
    }

    private int getCooldownDuration(String weaponId) {
        return switch (weaponId) {
            case "fire_blitz_sword" -> 40;
            case "lightning_axe"   -> 40;
            case "dash_mace"       -> 30;
            default                -> 30;
        };
    }

    public void removePlayer(UUID playerId) {
        cooldowns.remove(playerId);
    }
}
```

---

### src/main/java/com/benserver/weapons/listeners/WeaponAbilityListener.java

```java
package com.benserver.weapons.listeners;

import com.benserver.weapons.BenWeaponsPlugin;
import com.benserver.weapons.items.CustomWeapons;
import com.benserver.weapons.managers.CooldownManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        String weaponType = customWeapons.getWeaponType(player.getInventory().getItemInMainHand());
        if (weaponType == null) return;

        int cooldownLeft = cooldownManager.getCooldownSeconds(player, weaponType);
        if (cooldownLeft > 0) {
            player.sendMessage(ChatColor.RED + "⏳ Wait " + cooldownLeft + " more second"
                + (cooldownLeft == 1 ? "" : "s") + "!");
            return;
        }

        event.setCancelled(true);

        switch (weaponType) {
            case CustomWeapons.FIRE_SWORD_ID    -> activateFireBlitz(player);
            case CustomWeapons.LIGHTNING_AXE_ID -> activateLightningStrike(player);
            case CustomWeapons.DASH_MACE_ID     -> activateDash(player);
        }

        cooldownManager.setCooldown(player, weaponType);
    }

    // ── FIRE BLITZ: launch a fireball in the direction you're looking ──
    private void activateFireBlitz(Player player) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();

        Fireball fb = player.getWorld().spawn(eye.add(dir.clone().multiply(1.5)), Fireball.class);
        fb.setDirection(dir.multiply(2));
        fb.setShooter(player);
        fb.setYield(2.5f);
        fb.setIsIncendiary(true);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.8f);
        player.getWorld().spawnParticle(Particle.FLAME, eye, 20, 0.2, 0.2, 0.2, 0.05);
        player.sendMessage(ChatColor.RED + "🔥 Fire Blitz! " + ChatColor.GRAY + "(40s cooldown)");
    }

    // ── LIGHTNING STRIKE: hit what you're looking at, then get Regen II ──
    private void activateLightningStrike(Player player) {
        var target = player.getTargetBlockExact(30);
        Location strike = (target != null)
            ? target.getLocation().add(0.5, 0, 0.5)
            : player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(30));

        player.getWorld().strikeLightning(strike);

        // 20 seconds of Regeneration II as a reward for using the ability
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 1, false, true));

        player.sendMessage(ChatColor.AQUA + "⚡ Lightning Strike! "
            + ChatColor.GREEN + "Regen II for 20s. "
            + ChatColor.GRAY + "(40s cooldown)");
    }

    // ── DASH: launch yourself ~20 blocks forward ──
    private void activateDash(Player player) {
        Vector dir = player.getEyeLocation().getDirection();
        dir.setY(Math.max(dir.getY(), 0.1)); // Slight upward arc so you don't slam into the ground
        dir.normalize().multiply(1.8);        // Tune this number to adjust dash distance

        player.setVelocity(dir);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.3, 0.1, 0.3, 0.05);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "💨 Dash! " + ChatColor.GRAY + "(30s cooldown)");
    }
}
```

---

### src/main/java/com/benserver/weapons/tasks/PassiveEffectsTask.java

```java
package com.benserver.weapons.tasks;

import com.benserver.weapons.items.CustomWeapons;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PassiveEffectsTask extends BukkitRunnable {

    private final CustomWeapons customWeapons;

    public PassiveEffectsTask(CustomWeapons customWeapons) {
        this.customWeapons = customWeapons;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            customWeapons.applyPassiveEffects(player);
        }
    }
}
```

---

### src/main/java/com/benserver/weapons/commands/WeaponsCommand.java

```java
package com.benserver.weapons.commands;

import com.benserver.weapons.items.CustomWeapons;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WeaponsCommand implements CommandExecutor {

    private final CustomWeapons customWeapons;

    public WeaponsCommand(CustomWeapons customWeapons) {
        this.customWeapons = customWeapons;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage("Run this command in-game.");
            return true;
        }
        if (!senderPlayer.hasPermission("benweapons.give")) {
            senderPlayer.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        if (args.length == 0) {
            senderPlayer.sendMessage(ChatColor.GOLD + "Usage: /benweapons <sword|axe|mace> [player]");
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
```

---

## Build Instructions for Claude Code

Once all files are created, run from inside `~/minecrafting/ben-weapons/`:

```bash
mvn clean package
```

This will produce `target/BenWeapons.jar`.

**Then copy it to the workspace folder:**

```bash
cp target/BenWeapons.jar ~/minecrafting/BenWeapons.jar
```

---

## How Ben Installs It On His Server

1. Download `BenWeapons.jar` from this folder
2. Upload it to the `plugins/` folder on his server (via his server dashboard's file manager)
3. Restart the server
4. Done! The weapons will show up in the recipe book in-game

---

## The Three Weapons — Quick Summary

| Weapon | Crafting Ingredients | Ability (right-click) | Passive (in inventory) |
|--------|---------------------|----------------------|----------------------|
| ⚔ Fire Blitz Sword | Iron Sword + 2 Fire Charges + 2 Blaze Rods | Launches a fireball (40s cooldown) | Fire Resistance forever |
| ⚡ Lightning Axe | Iron Axe + 3 Gold Ingots + 1 Blaze Rod | Strikes lightning + gives Regen II for 20s (40s cooldown) | None |
| 💨 Dash Mace | Mace + 4 Ender Pearls | Dashes ~20 blocks forward (30s cooldown) | Speed II forever |

---

## Notes for Claude Code

- If compilation fails due to a version mismatch between the Paper API and the server version Ben is actually running, update the `<version>` tag in `pom.xml` under the `paper-api` dependency to match. Ben's server version can be found by running `version` in the server console.
- The `MACE` material was added in Minecraft 1.21 — if it shows as unresolved, the Paper API version in pom.xml may be too old.
- Dash distance can be tuned by adjusting the `multiply(1.8)` value in `WeaponAbilityListener.java` — higher = further.
