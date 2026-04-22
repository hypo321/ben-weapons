# How We Built Ben's Custom Minecraft Weapons Using AI

*Three different AI tools. One afternoon. Zero prior experience with Minecraft plugins. Here's exactly how it worked — what each tool did, where things went sideways, and what we learned.*

---

## The Brief

Ben had a clear idea of what he wanted. His server needed custom weapons — exclusive items that players could craft, not just receive. The kind of thing that makes your server feel different from every other one. The specs were clear, detailed, and exactly right:

> *"Sword: a fire blitz type thing that has a 40 sec cooldown not too much damage tho make it give u infinite fire resistance when u got it in ur inventory"*
>
> *"Axe: a lighting strike ability cooldown of 40 secs not too much damage still also when using the ability make it give you 20 sec of regeneration"*
>
> *"Mace: a dash mace not to big of a reach jus like 20 block dash with 30 sec cooldown and make it give infinite speed 2 when its in ur inventory"*

By the end of the same afternoon: all three weapons existed, were working on Ben's server, and had their own website at [github.com/hypo321/ben-weapons](https://github.com/hypo321/ben-weapons). The plugin was 16KB, had 11 Java files, and — this is the impressive bit — zero compile errors across the entire build process.

Neither of us had written a Minecraft server plugin before.

Here's how it actually happened.

---

## Three AI Tools, Three Different Jobs

Before getting into the story, it helps to understand that three different AI tools were involved, and each one was good at something different. Using the right one at the right moment was most of the trick.

**Claude Cowork** — think of this as the planning conversation. It's a chat-based AI that can read files, search the web, and help you think through what you're actually trying to build before anyone writes a line of code. It's the "what are we building and why?" tool.

**Claude Code** — this one runs inside your terminal and has access to your actual computer. It creates files, installs missing software, runs builds, reads error messages, and fixes things. It's the builder.

**Cascade (inside Windsurf)** — an AI that lives inside a code editor and sees your whole project at once. It's brilliant for ongoing development: adding features, debugging things that only break at runtime, managing releases. It's the long-term collaborator.

Each one had its own act in this story.

---

## Act 1 — Figuring Out What We're Actually Building (Cowork)

### Starting From the Photos

Ben sent three photos of his tablet screen showing the crafting mod interface. Before asking a single question, Cowork looked carefully at what was actually there.

The UI visible in those photos — the bordered recipe viewer with the ingredient search bar at the bottom — only exists in **Java Edition** Minecraft. Bedrock doesn't have anything like it. The search terms visible ("neth", "light", "quart") matched the weapon themes exactly. So straight away: Java Edition, modded, recent version.

That version question turned out to be more interesting than expected. The initial guess was "Java Edition 1.21+." When that was challenged, rather than doubling down, the AI searched for the current version:

```
Search: "Minecraft Java Edition latest version 2026"
```

Turns out Minecraft had completely scrapped its old versioning system. It wasn't "1.22-something." The current version was **26.1.2**, using a new format based on the year. That's the kind of thing AI gets wrong when it hasn't kept up with the world — its training data has a cutoff, and software keeps moving. The right move is to go and check rather than confidently state something out of date.

### What the Voice Notes Revealed

Ben's mum sent voice messages with more context. The AI tried to transcribe them automatically — this failed, the tools weren't available — so it asked for typed transcripts instead. When those arrived, the most important piece of information changed everything:

**Ben runs his own paid server.** He'd recently switched providers because the old one was slow. And the weapons weren't prizes to be handed out — they were crafting recipes. A perk of being on his server.

This completely changed the technical approach. There are two ways to add custom content to Minecraft:

- A **mod** — installed on every player's computer individually
- A **plugin** — installed once on the server, invisible to players, just works

Since Ben controls the server, a plugin was clearly right. Players join as normal and the custom weapons are just... there. No faff.

### The Spec Document

Rather than trying to compile code during the planning phase (which hit Java version problems anyway — more on those shortly), the Cowork session produced a different kind of output: a complete specification document called `BenWeapons-ClaudeCode-Spec.md`.

This wasn't just notes. It was the full Maven project structure, every Java source file with plain-English comments, the crafting recipes, and step-by-step build instructions. The comments were written so anyone reading the code later could follow what each part was doing:

```java
// --- Step 4: Start the passive effects checker (runs every second) ---
// This is what gives players Fire Resistance and Speed while holding the weapons
new PassiveEffectsTask(customWeapons).runTaskTimer(this, 0L, 20L);
```

The recipe layouts in the comments weren't needed by the compiler. They were there because they're easy to read:

```java
// Recipe:  [ _ ] [B] [ _ ]
//          [F]   [S] [F]
//          [ _ ] [B] [ _ ]
// B = Blaze Rod, F = Fire Charge, S = Iron Sword
```

**The takeaway from Act 1:** A conversational AI is great for working out what you're building and producing plans that other tools can act on. The handoff to the builder — with a proper spec — is what makes the next part go quickly.

---

## Act 2 — Building the Thing (Claude Code)

### From Empty Folder to Working Plugin in 15 Minutes

The spec was handed to Claude Code with one instruction:

> *"Please create this Maven project, compile it, and place the final BenWeapons.jar in ~/minecrafting/ ready to drop into a server's plugins/ folder."*

The first thing it did was check what was missing. Java 21 and Maven weren't installed. Rather than stopping and asking for help, it just sorted it:

```bash
brew install openjdk@21 maven
```

Then set up the build environment and compiled:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21 \
PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH" \
mvn clean package
```

**Fifteen minutes from an empty folder to a working JAR file.** That's what happens when an AI is allowed to fix the environment as part of the job, rather than stopping to ask permission at every step.

### Where Ben's Real Designs Were Different From the Spec

Then Ben sent photos of what he actually wanted the crafting recipes to look like.

The spec had placeholder recipes — simple cross patterns. Ben's real designs were far more involved: all nine crafting slots filled, Player Heads in every corner, Netherite Ingots, Breeze Rods, Heavy Cores. Properly rare ingredients for properly special weapons.

The first attempt at reading those photos produced a recipe using **Dried Ghast** — which only exists in Minecraft 1.21.5, while the server was targeting 1.21.4. Before writing a single line of code, Claude Code flagged it:

> *"DRIED_GHAST was added in Minecraft 1.21.5. The current pom.xml targets Paper API 1.21.4. If Ben's server is 1.21.4, this won't work."*

There was also a conflict in the recipe key — the letter `E` had been used to mean both Ender Pearl and Echo Shard at the same time:

> *"The key lists E=Ender Pearl AND E=Echo Shard — that's a conflict. Which one is it?"*

Once confirmed, the corrected recipe went in cleanly:

```java
fireSword.shape("HIH", "BQB", "HRH");
fireSword.setIngredient('H', Material.PLAYER_HEAD);
fireSword.setIngredient('I', Material.NETHERITE_INGOT);
fireSword.setIngredient('B', Material.BLAZE_POWDER);
fireSword.setIngredient('Q', Material.QUARTZ);
fireSword.setIngredient('R', Material.BLAZE_ROD);
```

Catching a version mismatch *before* the code is written costs nothing. Catching it after a failed build is annoying. Catching it after players have downloaded the wrong version is worse. This is one of the things AI is actually useful at — knowing what it doesn't know and saying so.

### One Small Change, Four Files

A request to change the cooldowns — mace to 15 seconds, sword to 30, axe to 25 — looked simple. It actually touched four different files:

| File | What changed |
|------|-------------|
| `CooldownManager.java` | The actual cooldown logic |
| `CustomWeapons.java` | The lore text players see when hovering over the item |
| `WeaponAbilityListener.java` | The message sent to the player when they use the ability |
| `BenWeapons-ClaudeCode-Spec.md` | The documentation |

An AI that understands the whole project finds all of those automatically and updates them in one go. This is genuinely useful — the number of bugs that exist purely because someone updated the logic but forgot to update the display text is enormous.

---

## Act 3 — Shipping, Growing, and Breaking Things (Cascade / Windsurf)

### The Plugin That Kept Growing

Cascade (inside the Windsurf editor) took the working plugin and built out the full project: a GitHub repo, versioned releases, and a public website. But the interesting part of Act 3 wasn't the website — it was how much the plugin itself grew beyond the original spec.

Ben knew exactly what he wanted, and the features came thick and fast:

| Feature | Why |
|---------|-----|
| **Trust system** (`/trust`, `/untrust`) | If your friend is on the server, you don't want to lightning-strike them every time they walk past |
| **One-per-server craft limit** | Only one of each weapon should ever exist — makes them actually rare |
| **Container lock** | Weapons can't be stashed in chests or hoppers — they stay with their owner |
| **Different activation triggers** | Axe fires on every hit automatically; Mace uses the F key; Sword uses `/sword ability activate` |
| **Netherite base items** | Upgraded from Iron Sword/Axe — if the ingredients are Netherite-tier, the weapon should look like it |
| **`/benweapons version`** | Turns out this was extremely useful (more on that shortly) |

The original code handled all of this growth cleanly because each class had been given exactly one job. Adding a `TrustManager` didn't require touching `CooldownManager`. Adding `CraftLimitManager` didn't require touching the weapon creation code. When things are organised properly up front, adding features later is much less painful.

### Building Features One Step at a Time

The trust system is a good example of how to do this well. It started as the simplest possible thing:

```java
// Version 1: just an in-memory list — no files, no complexity
private final Map<UUID, Set<UUID>> trustLists = new HashMap<>();
```

That's it. Just a map in memory. Then, once that worked, it gained persistence — saving to a file so the trust lists still exist after a server restart:

```java
public void load() {
    FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
    for (String key : config.getKeys(false)) {
        // restore from trust.yml...
    }
}
```

Then tab-completion on the command. Then permission fixes. Each step separately, each step tested before moving on.

"Add persistence to the trust system" is a good instruction. "Build a complete trust system with persistence, tab-completion, and permissions" is a worse one — too many things at once, too easy to get confused about which bit isn't working.

### When Pushing Back Made the Code Better

When the craft-once limit was first implemented, the AI tracked it per player — who had crafted which weapon. Then the requirement was clarified: it should be per *server*. Only one of each weapon should ever exist anywhere.

The AI restructured the whole thing. What had been a complicated nested data structure became something much simpler:

```java
// Before: complicated, tracked per player, also wrong
private final Map<String, Set<UUID>> craftedBy = new HashMap<>();

// After: one weapon crafted = it's gone forever, globally
private final Set<String> craftedWeapons = new HashSet<>();
```

Simpler *and* correct. This happens fairly often — when you push back and clarify what you actually mean, the AI finds a better solution than it would have come up with when guessing.

### Automating the Annoying Stuff

One detail that kept causing grief: the version number in `plugin.yml` kept getting out of sync with the actual release. Fix it manually every time, forget once, players download the wrong version.

The solution was to stop doing it manually at all. Maven can inject the version automatically at build time:

```xml
<!-- Only ever change this one number -->
<version>1.4.2</version>
```

```yaml
# plugin.yml — filled in automatically when the project builds
version: ${project.version}
```

When something keeps going wrong manually, that's usually a sign it should be automated. Worth asking the AI to sort it.

---

## Where It Actually Went Wrong

No honest writeup leaves this part out. There were three proper mistakes.

### The Wrong Diagnosis

Players reported that the `/trust` command wasn't working — it came back as "Unknown or incomplete command." The AI's first diagnosis: command conflicts with other plugins. It renamed everything to `benweapons-trust`, `benweapons-untrust`, and so on. A whole new version was released. Then immediately reverted.

The actual problem was two lines in a config file:

```yaml
benweapons.trust:
  default: true  # ← this was missing
```

That's it. The AI was trying to diagnose a runtime bug without the actual error output — that's guesswork. **Paste the real error message.** Describing what's happening in words is slower and usually leads down the wrong path.

### The Wrong Event

The Dash Mace needed to activate when you press the F key — Minecraft's "swap hands" key. The AI wired it to the wrong event — one that fires when you right-click the off-hand slot, not when you press F:

```java
// Wrong — triggers on right-click, not F key
public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getHand() != EquipmentSlot.OFF_HAND) return;
    ...
}
```

The correct one has a completely different name:

```java
// Correct — triggers specifically on F key press
public void onSwapHands(PlayerSwapHandItemsEvent event) {
    ...
}
```

Minecraft has dozens of events with similar-sounding names. The AI will sometimes pick the wrong one. Code review doesn't catch this — you have to actually press the button in-game and see if it does what you expect.

### The Stale JAR

The most painful one. For several releases, players were downloading an old version of the plugin. The JAR was being compiled correctly but not always copied to the right location before being uploaded to GitHub. The `/benweapons version` command — which should have returned the new version — kept returning `1.0.0`.

The fix: always verify the JAR before releasing it.

```bash
unzip -p BenWeapons.jar plugin.yml | grep version
```

If the version shown doesn't match what you're about to release, stop. Don't release it. This is why `/benweapons version` was actually the most useful thing in the whole project — it answered "is the right JAR loaded?" in about two seconds.

---

## What Each Tool Was Actually Good At

Looking back at everything, the three tools had genuinely different strengths:

**Cowork** was best for thinking before building — exploring what the problem actually was, translating a clear creative vision into a technical plan, figuring out what type of solution was needed, and producing a spec that the next tool could act on.

**Claude Code** was best for fast, end-to-end execution — taking a clear spec and making it real without needing hand-holding at each step. It fixed the environment as part of the job, caught version mismatches before they caused problems, and propagated changes across multiple files consistently.

**Cascade / Windsurf** was best for the longer game — iterating across many sessions, seeing how changes rippled through the whole codebase, managing releases, and finding simpler solutions when requirements got clarified.

None of them could test in-game. That part was always on Ben (or Paul). Running the code and honestly reporting what happened is still a human job.

---

## What to Take Away If You Want to Do This

If you want to build something similar — a plugin, an app, anything where you're starting from an idea and need working software — here's what made this go well:

Write a spec before touching any code. The more specific it is, the better the output. Vague brief, vague result.

Give the AI permission to sort the environment. If Java isn't installed, Maven is missing, whatever — let it fix that as part of the job.

Share the actual error. When something breaks, paste the real console output rather than describing it. "Unknown command error" is less useful than the actual stack trace.

Build one thing at a time. "Add persistence" is better than "add persistence, tab-completion, and permissions all at once."

Push back on the first answer. The craft limit system went from complicated and wrong to simple and correct because the requirement was properly explained. Don't just accept the first implementation.

Test it for real. Code review doesn't catch the wrong event. You have to actually press F and see what happens.

---

## The Numbers

| | |
|--|--|
| Time to first working JAR | ~15 minutes |
| Total time, spec to v1.4.2 | One afternoon |
| Final plugin file size | 16KB |
| Java source files | 11 |
| Lines of Java | ~600 |
| Compile errors | 0 |
| Versions released | v1.0.0 → v1.4.2 |
| Commands | `/benweapons`, `/sword`, `/trust`, `/untrust`, `/benweapons version` |

---

## What's Next

The plugin works. The website is live. The weapons are craftable on Ben's server.

The one thing explored but not yet built: **custom 3D models**. Right now the weapons look like whatever Minecraft item they're based on — Netherite Sword, Netherite Axe, Mace. Custom geometry would make them completely unique visually.

The vanilla Netherite Sword model is actually just this:

```json
{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/netherite_sword"
  }
}
```

That's a 2D sprite on a flat template. Not a 3D model at all. Building real custom geometry means Blockbench (free, web-based 3D modelling), exported as a resource pack that players download when they join. For animated weapons, GeckoLib handles that — Blockbench even has a GeckoLib export mode. It'd require players to have the client mod installed, but for a small private server that's fine.

That's a future session. For now, Ben has what he asked for.

---

## The Bit That Actually Mattered

What made this work wasn't any single AI tool. It was using each tool for what it's actually good at, at the right moment.

The creative vision was entirely Ben's. Three specific weapons with specific abilities. The anti-cheat server rules. The one-per-server rarity. The trust system so friends don't get fireballed. None of that came from an AI — it came from someone who knew what he wanted his server to feel like.

The AI did the technical translation. But it needed the vision to work from.

The Fire Blitz Sword launches fireballs. The Lightning Axe strikes on every hit. The Dash Mace sends you flying when you press F.

No cheaters were involved in the making of this plugin.

---

*Source code: [github.com/hypo321/ben-weapons](https://github.com/hypo321/ben-weapons)*
