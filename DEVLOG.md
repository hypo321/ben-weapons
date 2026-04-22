# Building a Minecraft Plugin with AI: A Practical Devlog

*How a complete beginner shipped a working Paper plugin using Claude Code — what worked, what didn't, and how we iterated towards a solution together.*

---

## Background

Ben wanted three exclusive custom weapons for his Minecraft server — items with unique abilities, passive effects, and crafting recipes that felt special. Neither of us had written a Paper (Minecraft Java server) plugin before. The goal was to go from zero to a working `.jar` file ready to drop into a server.

This article documents the real process: the spec, the builds, the mistakes, the corrections, and the things we learned along the way.

---

## 1. Starting with a Spec

The first move was to write a detailed spec file before touching any code. This turned out to be one of the most important decisions of the whole project.

**What worked:** Writing `BenWeapons-ClaudeCode-Spec.md` as a shared contract between human intent and AI execution. The spec included not just *what* to build, but the full intended file structure, exact Java source for each class, and explicit build instructions.

```
## Project Structure to Create

ben-weapons/
├── pom.xml
└── src/main/
    ├── resources/
    │   └── plugin.yml
    └── java/com/benserver/weapons/
        ├── BenWeaponsPlugin.java
        ├── commands/
        ├── items/
        ├── listeners/
        ├── managers/
        └── tasks/
```

**Key lesson:** The more specific the spec, the less ambiguity the AI has to resolve on its own. Vague prompts produce vague code. A spec that includes exact class names, method signatures, and file paths produces code that actually compiles.

**The first prompt was essentially:** *"Please create this Maven project, compile it, and place the final BenWeapons.jar in ~/minecrafting/ ready to drop into a server's plugins/ folder."*

One prompt. One working JAR.

---

## 2. What Broke Immediately

The build required **Java 21** and **Maven** — neither was installed.

Rather than stopping and asking for manual intervention, Claude Code handled this automatically:

```bash
brew install openjdk@21 maven
```

Then set `JAVA_HOME` correctly for the build:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21 PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH" mvn clean package
```

**Key lesson:** AI-assisted development works best when the AI is given *permission to solve problems*, not just *permission to write code*. Checking prerequisites and fixing the environment is part of the job.

---

## 3. The Spec vs. Reality Gap

The first JAR built and loaded cleanly. Then Ben sent photos.

The recipes in the spec were simple 3-item patterns:

```java
// Original (wrong)
fireSword.shape(" B ", "FSF", " B ");
fireSword.setIngredient('B', Material.BLAZE_ROD);
fireSword.setIngredient('F', Material.FIRE_CHARGE);
fireSword.setIngredient('S', Material.IRON_SWORD);
```

But Ben's photos showed all nine crafting slots filled — Player Heads in every corner, Netherite Ingots, Breeze Rods, Heavy Cores. The spec had been written with placeholder recipes, and the real design only existed in Ben's head.

**What didn't work:** Trying to identify ingredients from blurry photos alone. The first attempt at reading the photos produced a recipe with **Dried Ghast** — a 1.21.5-only material — despite the server targeting 1.21.4. It would have failed to compile.

**What did work:** Flagging the problem clearly before touching any code:

> *"DRIED_GHAST was added in Minecraft 1.21.5. The current pom.xml targets Paper API 1.21.4. If Ben's server is 1.21.4, this won't work."*

And flagging a key ambiguity in the spec:

> *"The key lists E=Ender Pearl AND E=Echo Shard — that's a conflict. E appears in middle row left and right. Which one is it?"*

**Key lesson:** Catching a version mismatch or a spec ambiguity *before* writing code is far cheaper than discovering it after a failed build. A good AI assistant should push back on unclear specs, not silently guess.

The corrected recipes — once confirmed — were a clean targeted edit:

```java
// Corrected
fireSword.shape("HIH", "BQB", "HRH");
fireSword.setIngredient('H', Material.PLAYER_HEAD);
fireSword.setIngredient('I', Material.NETHERITE_INGOT);
fireSword.setIngredient('B', Material.BLAZE_POWDER);
fireSword.setIngredient('Q', Material.QUARTZ);
fireSword.setIngredient('R', Material.BLAZE_ROD);
```

---

## 4. Small Changes, Many Files

A seemingly tiny request — *"change the cooldowns: mace 15s, sword 30s, axe 25s"* — actually touched **four files simultaneously**:

| File | What changed |
|------|-------------|
| `CooldownManager.java` | The actual cooldown durations |
| `CustomWeapons.java` | Item lore shown in inventory |
| `WeaponAbilityListener.java` | Message sent to player on activation |
| `BenWeapons-ClaudeCode-Spec.md` | Documentation |

```java
// CooldownManager.java — before
case "fire_blitz_sword" -> 40;
case "lightning_axe"   -> 40;
case "dash_mace"       -> 30;

// after
case "fire_blitz_sword" -> 30;
case "lightning_axe"   -> 25;
case "dash_mace"       -> 15;
```

**Key lesson:** In a well-structured codebase, constants and display strings tend to live in different places. An AI that understands the full project can propagate a change everywhere it needs to go in one pass, rather than leaving stale values in the lore text while updating the logic. Always keep spec/documentation in sync with code — let the AI do it in the same commit.

---

## 5. The Plugin Grew Beyond the Spec

The spec defined a minimal plugin. The actual shipped version was considerably richer — features were added iteratively between sessions:

| Feature | Why it was added |
|---------|-----------------|
| **Trust system** (`/trust`, `/untrust`) | Prevent weapons harming friends — a real PvP server concern |
| **One-per-server craft limit** | Stop the weapons being duplicated endlessly |
| **Container lock** | Weapons couldn't be stashed in chests/hoppers |
| **Different activation triggers** | Axe auto-triggers on hit; Mace uses F key; Sword uses `/sword` command |
| **Netherite base items** | Upgraded from Iron Sword/Axe to Netherite for visual match with rare ingredients |
| **`/benweapons version`** | Console-friendly version check for confirming correct JAR is loaded |

The architecture evolved cleanly because the original structure had good separation of concerns:

```
BenWeaponsPlugin
├── CooldownManager       — in-memory cooldown tracking
├── CustomWeapons         — item factory + recipe registration
├── TrustManager          — persisted player trust relationships
├── CraftLimitManager     — persisted one-time craft tracking
└── Listeners
    ├── WeaponAbilityListener  — all three weapon triggers
    ├── InventoryListener      — container restriction
    └── CraftLimitListener     — crafting gate
```

**Key lesson:** Starting with clean separation of concerns pays off when requirements grow. Adding a `TrustManager` didn't require touching `CooldownManager` or `CustomWeapons` — each class had a single clear job.

---

## 6. Running the Server

Setting up a local Paper server to test the plugin was a single session:

```bash
# Fetch the latest stable build number from the Paper API
curl -s "https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds" \
  | python3 -c "..."  # → build 232

# Download it
curl -L -o paper-1.21.4-232.jar \
  "https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/232/downloads/paper-1.21.4-232.jar"

# Accept EULA, write start script, copy plugin, launch
```

Output on first boot:
```
[BenWeapons] ╔══════════════════════════════════════╗
[BenWeapons] ║   Ben's Custom Weapons — Loaded!     ║
[BenWeapons] ║   Fire Blitz Sword  ✓                ║
[BenWeapons] ║   Lightning Axe     ✓                ║
[BenWeapons] ║   Dash Mace         ✓                ║
[BenWeapons] ╚══════════════════════════════════════╝
Done (21.747s)! For help, type "help"
```

**What didn't work (initially):** Sending console commands to the background server process — there's no stdin access to a background shell task. The fix was to stop the server, enable RCON in `server.properties`, restart, and use a small Python RCON client:

```python
def rcon(host, port, password, commands):
    s = socket.socket()
    s.connect((host, port))
    # ... auth + send commands via RCON protocol
    
rcon('127.0.0.1', 25575, 'benserver123', [
    'op Hypo360',
    'benweapons sword Hypo360',
])
```

```
$ op Hypo360
  → Made HYPO360 a server operator
```

**Key lesson:** When one approach hits a wall (no stdin to background process), pivot to a standard protocol rather than fighting the limitation. RCON is exactly what it's there for.

---

## 7. The GitHub Pages Site

The project got a proper website — `docs/index.html` with crafting grids, weapon cards, installation steps, and a command reference — built to match the plugin's design language (dark theme, orange/cyan/purple accent colours per weapon).

The article you're reading now lives at `docs/article.html` and is linked from the site footer.

All links in the article point to real files in the repository:
- Source files link to `github.com/hypo321/ben-weapons/blob/main/...`
- The download button links to the latest GitHub Release JAR

**Key lesson:** Documentation written alongside the code, in the same repository, stays accurate. When the recipes changed, the spec, the code, and the website all updated in the same session.

---

## 8. What We Explored but Didn't Build (Yet)

**Custom 3D models via Blockbench** — we explored this. The vanilla netherite sword model is just:

```json
{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/netherite_sword"
  }
}
```

It's a flat 2D sprite rendered by a parent template, not a 3D model at all. Building custom geometry in Blockbench is the right tool, but requires:
1. Extracting vanilla assets from the Minecraft JAR (or `mcasset.cloud` without the client installed)
2. Loading the model in Blockbench and editing it
3. Exporting a resource pack and hosting it for players to download

For **animated** models, GeckoLib (a Paper plugin + client mod) is the proper solution — Blockbench has a GeckoLib export mode built in. Requires players to install the client mod, which is practical for a small private server.

**This work is parked for a future session.**

---

## Key Takeaways

| # | Lesson |
|---|--------|
| 1 | **Write a spec first.** The more specific, the better the output. |
| 2 | **Give the AI permission to fix the environment**, not just write code. |
| 3 | **Flag ambiguities before coding,** not after. Version mismatches and key conflicts are cheap to catch early. |
| 4 | **Propagate changes everywhere in one pass** — logic, lore text, messages, and documentation. |
| 5 | **Clean architecture pays off** when requirements grow unexpectedly. |
| 6 | **When one approach hits a wall, use the standard protocol** — RCON exists for exactly this. |
| 7 | **Keep docs in the same repo as code.** They stay in sync automatically. |
| 8 | **The spec is a living document.** Update it every time the code changes. |

---

## The Numbers

| Metric | Value |
|--------|-------|
| Time from empty directory to first working JAR | ~15 minutes |
| Total session time to v1.4.2 | One afternoon |
| Final JAR size | 16KB |
| Java files | 11 |
| Commands implemented | 5 (`/benweapons`, `/sword`, `/trust`, `/untrust`, + version) |
| Lines of Java | ~600 |
| Compile errors during development | 0 |

The final plugin is at [github.com/hypo321/ben-weapons](https://github.com/hypo321/ben-weapons).
