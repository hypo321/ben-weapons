# ⚔ Ben's Custom Weapons — Minecraft Plugin

A [Paper](https://papermc.io/) Minecraft server plugin that adds **three craftable custom weapons** with unique abilities and passive effects. Built for Paper 1.21.4, requiring Java 21+.

> 🌐 **[View the full weapon guide →](https://hypo321.github.io/ben-weapons/)**

---

## The Three Weapons

| Weapon | Ability (right-click) | Cooldown | Passive |
|--------|----------------------|----------|---------|
| ⚔ **Fire Blitz Sword** | Launches a fireball | 30s | Fire Resistance (always active) |
| ⚡ **Lightning Axe** | Strikes lightning + grants Regen II for 20s | 25s | None |
| 💨 **Dash Mace** | Dashes ~20 blocks forward | 15s | Speed II (always active) |

---

## Crafting Recipes

### ⚔ Fire Blitz Sword

```
[Player Head]  [Netherite Ingot] [Player Head]
[Blaze Powder] [Nether Quartz]   [Blaze Powder]
[Player Head]  [Blaze Rod]       [Player Head]
```

### ⚡ Lightning Axe

```
[Player Head] [Netherite Axe] [Player Head]
[Gold Ingot]  [Lightning Rod] [Gold Ingot]
[Player Head] [Lightning Rod] [Player Head]
```

### 💨 Dash Mace

```
[Player Head]    [Heavy Core]      [Player Head]
[Echo Shard]     [Netherite Ingot] [Echo Shard]
[Player Head]    [Breeze Rod]      [Player Head]
```

---

## Installation

1. Download `BenWeapons.jar` from the [Releases](https://github.com/hypo321/ben-weapons/releases) page
2. Drop it into your server's `plugins/` folder
3. Restart the server
4. The weapons will appear in the in-game recipe book automatically

**Requirements:** Paper 1.21.4+ · Java 21+

---

## Admin Command

```
/benweapons <sword|axe|mace> [player]
```

Requires the `benweapons.give` permission (OP by default). Gives the specified weapon to the target player (or yourself if no player is specified).

---

## Building from Source

**Prerequisites:** Java 21+, Maven

```bash
git clone https://github.com/hypo321/ben-weapons.git
cd ben-weapons/ben-weapons
mvn clean package
# Output: target/BenWeapons.jar
```

---

## Compatibility

- **Server:** Paper 1.21.4+
- **Java:** 21+
- **Note:** The Mace material was introduced in 1.21 — earlier versions are not supported.

---

*Made with ❤️ for Ben's server.*
