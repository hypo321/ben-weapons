# AI-Assisted Minecraft Plugin Development: A Case Study

## Overview

This article documents a real development session building **BenWeapons** — a custom Minecraft Paper plugin — entirely through conversation with an AI coding assistant. The plugin grew from a simple weapon mod into a fully-featured system with persistence, permissions, crafting limits, server-wide announcements, and a public website. The journey illustrates both the strengths and pitfalls of AI-assisted development.

---

## The Project

A Paper 1.21.4 Minecraft plugin adding three craftable custom weapons:
- **Fire Blitz Sword** — launches a fireball via `/sword ability activate`
- **Lightning Axe** — auto-strikes lightning on every hit
- **Dash Mace** — launches the player ~30 blocks forward on F key

Features added across the session: trust system, inventory restrictions, crafting limits, server announcements, a GitHub Pages documentation site, and persistent data files.

---

## What Worked Well

### 1. Incremental feature building
Each feature was added in small steps without disturbing working code. For example, the trust system started as an in-memory `HashMap`, then gained persistence via `trust.yml`, then tab-completion, then permission fixes — each as a separate iteration.

```java
// Started simple — just an in-memory map
private final Map<UUID, Set<UUID>> trustLists = new HashMap<>();

// Then evolved to read/write YAML on disk
public void load() {
    FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
    for (String key : config.getKeys(false)) {
        // restore from file...
    }
}
```

**Lesson:** Ask the AI to implement one thing at a time. "Add persistence to the trust system" is better than "build a full trust system with persistence, tab-completion, and permissions."

### 2. The AI caught its own mistakes
When the craft-once limit was implemented per-player, the user clarified it should be per-server. The AI restructured the entire `CraftLimitManager` from a `Map<String, Set<UUID>>` down to a simple `Set<String>` — a much cleaner solution:

```java
// Before: tracked per player (wrong)
private final Map<String, Set<UUID>> craftedBy = new HashMap<>();

// After: server-wide (correct, and simpler)
private final Set<String> craftedWeapons = new HashSet<>();
```

**Lesson:** Don't accept the first implementation. Clarifying requirements often leads the AI to a simpler design.

### 3. Using Maven resource filtering to sync versions
A recurring bug was the `plugin.yml` version getting out of sync with the actual release. The fix was elegant — let Maven inject it at build time:

```xml
<!-- pom.xml: only need to update this one number -->
<version>1.4.2</version>
```

```yaml
# plugin.yml: automatically filled at build time
version: ${project.version}
```

**Lesson:** When you notice a repeated manual step that keeps going wrong, ask the AI to automate it.

---

## What Didn't Work (And Why)

### 1. Wrong root cause diagnosis
When players reported "Unknown or incomplete command" for `/trust`, the AI first blamed command conflicts with other plugins and renamed all the commands to `benweapons-trust`, `benweapons-untrust` etc. This was wrong — the real cause was simply a missing `default: true` permission in `plugin.yml`. An unnecessary version (v1.3.4) was released and then immediately reverted.

```yaml
# The actual fix — just two lines
benweapons.trust:
  default: true  # ← this was missing
```

**Lesson:** AI diagnosis of runtime bugs without server logs is guesswork. Always get the actual error output first.

### 2. Wrong event for F key detection
The Dash Mace was bound to `PlayerInteractEvent` with `OFF_HAND` — which fires on right-click, not the F key. The correct event is `PlayerSwapHandItemsEvent`. This worked fine in code review but only surfaced when actually tested in-game.

```java
// Wrong — fires on right-click of off-hand slot
public void onPlayerInteract(PlayerInteractEvent event) {
    if (!event.getAction().isRightClick()) return;
    if (event.getHand() != EquipmentSlot.OFF_HAND) return;
    ...
}

// Correct — fires specifically on F key press
public void onSwapHands(PlayerSwapHandItemsEvent event) {
    ...
}
```

**Lesson:** Minecraft has many similar-sounding events. AI will sometimes pick the wrong one. Test every interaction in-game.

### 3. The stale JAR problem
The most painful recurring issue: the wrong JAR kept being uploaded to GitHub releases. The `cp` command path was inconsistent — sometimes run from `ben-weapons/` using `../../BenWeapons.jar`, sometimes from the repo root. The result was that players downloaded a JAR with `version: 1.0.0` for multiple releases.

The fix was two things:
- Always build and copy from the repo root using `mvn -f ben-weapons/pom.xml`
- Verify the JAR before every release: `unzip -p BenWeapons.jar plugin.yml | grep version`

**Lesson:** Add a verification step after every build. Don't trust that the file was copied correctly — check it.

---

## Effective Patterns for AI-Assisted Development

### Show, don't tell
Pasting the actual console output or file contents got faster results than describing the problem in words. When the user pasted the plugin.yml from inside the JAR showing `version: 1.0.0`, the AI immediately identified the root cause.

### Ask for a plan first
For the craft announcement feature, asking "can we plan it first?" led to a useful design discussion about format, colours, and sounds before writing a single line of code. This avoided a rewrite.

### Use version numbers as a sanity check
Adding `/benweapons version` — a command that reads the version at runtime — became the single most useful debugging tool. It answered "is the right JAR loaded?" in two seconds.

```java
if (args[0].equalsIgnoreCase("version")) {
    String version = plugin.getDescription().getVersion();
    sender.sendMessage(ChatColor.GOLD + "BenWeapons" + ChatColor.GRAY + " v" + version);
    return true;
}
```

### Small releases, often
Releasing after every feature (v1.3.0 → v1.3.1 → ... → v1.4.2) made it easy to identify exactly which change introduced a bug. It also gave the end user something to test after every fix.

---

## Final Architecture

```
ben-weapons/
├── commands/
│   ├── WeaponsCommand.java   # /benweapons give + version
│   ├── SwordCommand.java     # /sword ability activate
│   └── TrustCommand.java     # /trust + /untrust (with tab-complete)
├── listeners/
│   ├── WeaponAbilityListener.java  # F key dash, auto-axe, fireball
│   ├── CraftLimitListener.java     # One-per-server limit + announcement
│   └── InventoryListener.java      # Block storing weapons in containers
├── managers/
│   ├── CooldownManager.java        # Per-player ability cooldowns
│   ├── TrustManager.java           # Trust lists → trust.yml
│   └── CraftLimitManager.java      # Crafted weapons → crafted.yml
├── items/
│   └── CustomWeapons.java          # Item creation + recipe registration
└── tasks/
    └── PassiveEffectsTask.java     # Fire resistance, Speed II every second
```

---

## Key Takeaway

AI is a strong pair programmer for Minecraft plugin development — it knows the Bukkit/Paper API well, catches obvious mistakes, and iterates quickly. But it cannot test in-game, and it can confidently pick the wrong event, wrong path, or wrong root cause. The human's job is to run the code, report back exactly what happened, and push back when the diagnosis doesn't feel right. The best results came when the conversation was a genuine back-and-forth — not just accepting the first answer.
