# ModLoader999

Welcome to the official documentation for ModLoader999, a powerful PaperMC plugin that allows you to create and load custom content into your Minecraft server with ease.

## For Server Owners

### Installation

1.  **Download:** Get the latest `ModLoader999-x.x.x.jar` from the official release page.
2.  **Install:** Place the downloaded `.jar` file into your server's `plugins` folder.
3.  **Restart:** Start or restart your server to generate the necessary configuration files and folders.
4.  **Add Mods:** Place your desired mod files (with a `.modloader999` extension) into the `plugins/ModLoader999/Mods/` directory.
5.  **Reload:** Restart your server or use the command `/modloader reload` to load the new mods.

## For Mod Developers

This guide provides a comprehensive overview of how to create your own mods using the ModLoader999 API.

### Project Setup

To begin, you'll need to set up a Java project using either Maven or Gradle.

#### Maven

1.  **Add Repositories:** Add the PaperMC and ModLoader999 repositories to your `pom.xml`:

    ```xml
    <repositories>
        <repository>
            <id>papermc</id>
            <url>https://repo.papermc.io/repository/maven-public/</url>
        </repository>
        <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/erik99948/modloader999</url>
        </repository>
    </repositories>
    ```

2.  **Add Dependencies:** Add the Paper API and ModLoader999 API as dependencies:

    ```xml
    <dependencies>
        <dependency>
            <groupId>io.papermc.paper</groupId>
            <artifactId>paper-api</artifactId>
            <version>1.21.1-R0.1-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>io.github.erik99948</groupId>
            <artifactId>modloader</artifactId>
            <version>1.1.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
    ```

#### Gradle

1.  **Add Repositories:** Add the following to your `build.gradle` file:

    ```gradle
    repositories {
        mavenCentral()
        maven { url 'https://repo.papermc.io/repository/maven-public/' }
        maven { url 'https://maven.pkg.github.com/erik99948/modloader999' }
    }
    ```

2.  **Add Dependencies:**

    ```gradle
    dependencies {
        compileOnly 'io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT'
        compileOnly 'io.github.erik99948:modloader:1.1.0'
    }
    ```

### The `modinfo.json` File

Every mod must contain a `modinfo.json` file in the `src/main/resources` directory. This file provides essential metadata about your mod.

```json
{
  "main": "com.yourname.mod.MyMod",
  "name": "MyFirstMod",
  "version": "1.0.0",
  "author": "YourName",
  "dependencies": {
    "AnotherMod": "^1.2.0"
  }
}
```

*   `main`: The fully qualified name of your main class that implements `ModInitializer`.
*   `name`: The unique name of your mod.
*   `version`: Your mod's version (should follow SemVer).
*   `author`: Your name.
*   `dependencies` (Optional): A map of other mods that your mod depends on, with their required version range.

### The `ModInitializer` Class

Your mod's main class must implement the `ModInitializer` interface. The `onLoad` method is the entry point for your mod, where you will register all of your custom content.

```java
package com.yourname.mod;

import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModInitializer;

public class MyMod implements ModInitializer {
    @Override
    public void onLoad(ModAPI api) {
        System.out.println("My First Mod is loading!");
        // All registration logic goes here
    }
}
```

### Registering Content: A Deep Dive

The `ModAPI` object provided in the `onLoad` method is your gateway to all of ModLoader999's features.

#### Custom Items

Custom items are created using standard Bukkit `ItemStack`s. The key is to use `CustomModelData` to link the item to a custom texture in your resource pack.

```java
// In onLoad(ModAPI api)
ItemStack myItem = new ItemStack(Material.DIAMOND_SWORD);
ItemMeta meta = myItem.getItemMeta();
meta.setDisplayName("§6Legendary Sword");
meta.setLore(Arrays.asList("A sword of great power."));
meta.setCustomModelData(1337);
myItem.setItemMeta(meta);

api.registerItem("legendary_sword", myItem);
```

#### Custom Blocks

Custom blocks are defined by a `CustomBlock` object, which specifies the base material, `CustomModelData`, and optional behaviors. Behaviors allow you to define custom logic for when a block is placed, broken, interacted with, receives redstone power, or explodes.

```java
// In onLoad(ModAPI api)
CustomBlock myBlock = new CustomBlock(
    "magic_crystal", // Unique ID for your block
    Material.GLASS,    // The base material this custom block will look like
    1338,              // CustomModelData for resource pack texture
    "§bMagic Crystal", // Display name
    Arrays.asList("A crystal that hums with energy."), // Lore
    (event, block, player) -> player.sendMessage("You placed a magic crystal!"), // BlockPlaceBehavior
    (event, block, player) -> {
        player.sendMessage("You broke a magic crystal!");
        // Custom break logic here, e.g., drop custom items
    }, // BlockBreakBehavior
    (event, block, player) -> player.sendMessage("You interacted with a magic crystal!"), // BlockInteractBehavior
    (event, block, oldCurrent, newCurrent) -> {
        if (newCurrent > 0 && oldCurrent == 0) {
            block.getWorld().strikeLightningEffect(block.getLocation());
        }
    }, // BlockRedstoneBehavior
    (event, block, blockList, yield) -> {
        block.getWorld().createExplosion(block.getLocation(), 2.0f, false, false);
        event.setCancelled(true); // Prevent default explosion behavior for this block
    }, // BlockExplodeBehavior
    Arrays.asList(new ItemStack(Material.DIAMOND)) // Custom drops when broken
);
api.registerBlock(myBlock);
```

#### Custom Mobs

Custom mobs are based on existing Minecraft entities. You can customize their stats, appearance, and behavior using `CustomMobGoal`s. You can also define custom spawners for natural generation.

```java
// In onLoad(ModAPI api)

// 1. Define Custom Mob Goals (optional)
List<CustomMobGoal> goals = Arrays.asList(
    new CustomMobGoal() { // Example: Simple follow goal
        @Override
        public boolean shouldStart(LivingEntity entity) {
            // Start this goal if the mob has a target and is far from it
            return entity.getTarget() != null && entity.getTarget().getLocation().distance(entity.getLocation()) > 3;
        }

        @Override
        public void start(LivingEntity entity) {
            entity.getWorld().playSound(entity.getLocation(), org.bukkit.Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 1.0f);
            entity.sendMessage("I see you!");
        }

        @Override
        public void stop(LivingEntity entity) {
            entity.sendMessage("Lost target.");
        }

        @Override
        public void tick(LivingEntity entity) {
            // Move towards the target
            entity.getNavigation().moveTo(entity.getTarget(), 1.2); // 1.2 is speed multiplier
        }
    }
);

// 2. Define the Custom Mob
CustomMob myMob = new CustomMob(
    "goblin_warrior", // Unique ID for your mob
    org.bukkit.entity.EntityType.ZOMBIE, // Base entity type (e.g., ZOMBIE, SKELETON)
    "§aGoblin Warrior", // Display name
    25.0, // Max Health
    4.0,  // Attack Damage
    0.35, // Movement Speed
    1339, // CustomModelData for resource pack model/texture
    goals, // List of custom goals
    null // Custom Attributes (e.g., Map.of(Attribute.GENERIC_ARMOR, 5.0))
);
api.registerMob(myMob);

// 3. (Optional) Register a Custom Mob Spawner for natural generation
api.registerMobSpawner((world, random, block) -> {
    // Spawn a goblin warrior in forests with a 5% chance
    if (block.getBiome() == org.bukkit.block.Biome.FOREST && random.nextInt(100) < 5) {
        // Spawn the mob at the block's location
        LivingEntity spawnedMob = (LivingEntity) api.getCustomMobRegistry().spawn("goblin_warrior", block.getLocation());
        if (spawnedMob != null) {
            spawnedMob.setTarget(world.getPlayers().get(0)); // Example: target nearest player
        }
    }
    return null; // Return null or a list of spawned entities
});
```

#### Custom Commands

Register commands with tab completion support. Your `ModCommandExecutor` will handle both command execution and tab completion suggestions.

```java
// In onLoad(ModAPI api)
api.registerCommand("modhelp", new ModCommandExecutor() {
    @Override
    public boolean onCommand(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§a--- My Mod Help ---");
            sender.sendMessage("§e/modhelp info - Get mod info");
            sender.sendMessage("§e/modhelp status - Check mod status");
            return true;
        }
        String subCommand = args[0].toLowerCase();
        if (subCommand.equals("info")) {
            sender.sendMessage("§aMy Mod Version: 1.0.0");
        } else if (subCommand.equals("status")) {
            sender.sendMessage("§aMy Mod is running smoothly!");
        } else {
            sender.sendMessage("§cUnknown subcommand. Use /modhelp.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("info", "status").stream()
                         .filter(s -> s.startsWith(args[0].toLowerCase()))
                         .collect(Collectors.toList());
        }
        return null;
    }
});
```

#### Event Listeners

Register standard Bukkit event listeners to react to in-game events. Any class implementing `org.bukkit.event.Listener` can be registered.

```java
// In onLoad(ModAPI api)
api.registerListener(new Listener() {
    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        event.getPlayer().sendMessage("§bWelcome, " + event.getPlayer().getName() + "! This server is running a cool mod!");
    }

    @EventHandler
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.STONE) {
            event.getPlayer().sendMessage("§7You broke some stone!");
        }
    }
});
```

#### Custom Recipes

Register custom crafting recipes for your items. You can use `ShapedRecipe`, `ShapelessRecipe`, or `FurnaceRecipe`.

```java
// In onLoad(ModAPI api)
// Assumes 'myItem' is the ItemStack for "legendary_sword" from a previous registration
NamespacedKey key = new NamespacedKey(JavaPlugin.getProvidingPlugin(getClass()), "legendary_sword_recipe");
ShapedRecipe recipe = new ShapedRecipe(key, myItem);
recipe.shape(" D ", " D ", " S "); // D = Diamond, S = Stick
recipe.setIngredient('D', Material.DIAMOND);
recipe.setIngredient('S', Material.STICK);
api.registerRecipe(recipe);

// Example: Shapeless recipe for 9 dirt from 1 grass block
NamespacedKey dirtKey = new NamespacedKey(JavaPlugin.getProvidingPlugin(getClass()), "dirt_from_grass");
ShapelessRecipe dirtRecipe = new ShapelessRecipe(dirtKey, new ItemStack(Material.DIRT, 9));
dirtRecipe.addIngredient(Material.GRASS_BLOCK);
api.registerRecipe(dirtRecipe);
```

#### Custom Enchantments

Create unique enchantments. Note that you must implement the enchantment's effects yourself using event listeners, as ModLoader999 only handles the registration of the enchantment type.

```java
// 1. Create your CustomEnchantment class
// (This class should be in your mod's package, e.g., com.yourname.mod)
package com.yourname.mod;

import com.example.modloader.CustomEnchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class LifestealEnchantment extends CustomEnchantment {
    public LifestealEnchantment(JavaPlugin plugin) {
        super(plugin, "lifesteal", "Lifesteal", 3, EnchantmentTarget.WEAPON, false, false);
    }

    @Override
    public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
        return other.equals(org.bukkit.enchantments.Enchantment.SHARPNESS);
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return item.getType() == Material.DIAMOND_SWORD || item.getType() == Material.NETHERITE_SWORD;
    }
}

// 2. Register it in your ModInitializer's onLoad(ModAPI api) method
// In onLoad(ModAPI api)
CustomEnchantmentAPI enchantmentAPI = api.getCustomEnchantmentAPI();
enchantmentAPI.registerEnchantment(new LifestealEnchantment(JavaPlugin.getProvidingPlugin(getClass())));

// 3. Implement the enchantment's effects using an Event Listener
api.registerListener(new Listener() {
    @EventHandler
    public void onDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LivingEntity && event.getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            ItemStack weapon = attacker.getEquipment().getItemInMainHand();

            LifestealEnchantment lifesteal = (LifestealEnchantment) enchantmentAPI.getEnchantment("lifesteal");
            if (lifesteal != null && weapon.containsEnchantment(lifesteal)) {
                int level = weapon.getEnchantmentLevel(lifesteal);
                double healAmount = event.getDamage() * (0.05 * level); // Heal 5% per level
                attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + healAmount));
                attacker.sendMessage("§aYou lifesteal " + String.format("%.1f", healAmount) + " health!");
            }
        }
    }
});
```

#### Custom Potion Effects

Define custom potion effects. Like enchantments, you'll need to implement the actual effects yourself.

```java
// 1. Create your CustomPotionEffectType class
// (This class should be in your mod's package, e.g., com.yourname.mod)
package com.yourname.mod;

import com.example.modloader.CustomPotionEffectType;
import org.bukkit.Color;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

public class PhotosynthesisEffect extends CustomPotionEffectType {
    public PhotosynthesisEffect(JavaPlugin plugin) {
        super(plugin, "photosynthesis", "Photosynthesis", Color.fromRGB(100, 200, 50), false, null);
    }
}

// 2. Register it in your ModInitializer's onLoad(ModAPI api) method
// In onLoad(ModAPI api)
CustomPotionEffectAPI potionAPI = api.getCustomPotionEffectAPI();
potionAPI.registerPotionEffectType(new PhotosynthesisEffect(JavaPlugin.getProvidingPlugin(getClass())));

// 3. Implement the potion effect's logic (e.g., using a BukkitRunnable)
new org.bukkit.scheduler.BukkitRunnable() {
    @Override
    public void run() {
        PhotosynthesisEffect photosynthesis = (PhotosynthesisEffect) potionAPI.getPotionEffectType("photosynthesis");
        if (photosynthesis == null) return;

        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (player.hasPotionEffect(photosynthesis)) {
                // Check if player is in sunlight
                if (player.getWorld().isDayTime() && player.getLocation().getBlock().getLightFromSky() == 15) {
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1)); // Restore food
                    player.setSaturation(Math.min(20, player.getSaturation() + 1)); // Restore saturation
                }
            }
        }
    }
}.runTaskTimer(JavaPlugin.getProvidingPlugin(getClass()), 0L, 40L); // Run every 2 seconds
```

#### World Generation

ModLoader999 provides extensive tools to customize world generation, including custom ore, tree, structure, and general populators. You can target specific worlds and biomes.

```java
// In onLoad(ModAPI api)

// Register a custom ore to generate in specific biomes (e.g., mountains)
api.registerOreGenerator((world, random, chunkX, chunkZ) -> {
    // Generate a custom ore (e.g., Material.EMERALD_ORE) 10% of the time
    if (random.nextInt(10) == 0) {
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = random.nextInt(60) + 60; // Generate between y=60 and y=120
        world.getBlockAt(x, y, z).setType(Material.EMERALD_ORE);
    }
}, null, org.bukkit.block.Biome.WINDSWEPT_HILLS, org.bukkit.block.Biome.SNOWY_SLOPES);

// Register a general world populator to add flower patches in plains biomes of the "world" world
api.registerWorldPopulator((world, random, chunk) -> {
    if (random.nextInt(5) == 0) { // 20% chance per chunk
        int x = random.nextInt(16);
        int z = random.nextInt(16);
        int y = chunk.getHighestBlockYAt(x, z); // Get highest block at this x,z
        chunk.getBlock(x, y, z).setType(Material.LILAC);
    }
}, new String[]{"world"}, org.bukkit.block.Biome.PLAINS);

// Register a custom tree generator for a specific world
api.registerTreeGenerator((world, random, x, y, z) -> {
    if (random.nextInt(50) == 0) { // 2% chance per tree attempt
        // Example: Generate a simple 2-block high custom tree
        world.getBlockAt(x, y, z).setType(Material.OAK_LOG);
        world.getBlockAt(x, y + 1, z).setType(Material.OAK_LOG);
        world.getBlockAt(x, y + 2, z).setType(Material.OAK_LEAVES);
    }
}, new String[]{"my_custom_world"}, null);

// Register a custom structure generator (e.g., a small ruin)
api.registerStructureGenerator((world, random, chunkX, chunkZ) -> {
    if (random.nextInt(200) == 0) { // 0.5% chance per chunk
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = world.getHighestBlockYAt(x, z) - 1; // Spawn on ground

        // Simple 3x3 stone platform
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.getBlockAt(x + dx, y, z + dz).setType(Material.COBBLESTONE);
            }
        }
        return true;
    }
    return false;
}, null, org.bukkit.block.Biome.DESERT);

// Advanced: Register a CustomChunkGenerator for a new world
// This gives you full control over how chunks are generated.
// You would typically define MyFlatWorldGenerator in your mod.
// api.getCustomWorldGeneratorAPI().registerCustomChunkGenerator("my_flat_world", new MyFlatWorldGenerator());
```

#### Custom Inventories (GUIs)

Create interactive GUIs for your players. You can define click and close handlers for custom behavior.

```java
// In onLoad(ModAPI api)
CustomInventoryAPI inventoryAPI = api.getCustomInventoryAPI();
Inventory myInventory = inventoryAPI.createInventory(27, "My Mod's GUI"); // 27 slots, custom title

inventoryAPI.setItem(myInventory, 13, new ItemStack(Material.EMERALD)); // Set item in middle slot

inventoryAPI.registerClickHandler(myInventory, event -> {
    event.setCancelled(true); // Prevent players from taking items
    if (event.getSlot() == 13) {
        event.getWhoClicked().sendMessage("§aYou clicked the emerald! Here's a reward.");
        event.getWhoClicked().getInventory().addItem(new ItemStack(Material.GOLD_INGOT));
        event.getWhoClicked().closeInventory();
    }
});

inventoryAPI.registerCloseHandler(myInventory, event -> {
    event.getPlayer().sendMessage("§7You closed the custom GUI.");
});

// To open this inventory for a player (e.g., when they run a command):
// Player player = (Player) sender;
// inventoryAPI.openInventory(player, myInventory);
```

#### Custom Particles

Spawn various particle effects in the world.

```java
// In onLoad(ModAPI api)
CustomParticleAPI particleAPI = api.getCustomParticleAPI();

// Example: Spawn 10 redstone particles at a player's location
// particleAPI.spawnParticle(player.getWorld(), Particle.REDSTONE, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1, new Particle.DustOptions(Color.RED, 1.0f));

// Example: Spawn a burst of flames at a specific location
// Location burstLocation = new Location(world, 100, 64, 100);
// particleAPI.spawnParticle(burstLocation.getWorld(), Particle.FLAME, burstLocation, 50, 1.0, 1.0, 1.0, 0.05);
```

#### Custom Sounds

Play custom sounds defined in your resource pack, or vanilla Minecraft sounds.

```java
// In onLoad(ModAPI api)
CustomSoundAPI soundAPI = api.getCustomSoundAPI();

// Example: Play a vanilla sound at a location
// soundAPI.playSound(player.getWorld(), Sound.ENTITY_PLAYER_LEVELUP, player.getLocation(), 1.0f, 1.0f);

// Example: Play a custom sound (defined in your resource pack's sounds.json) at a location
// soundAPI.playCustomSound(player.getWorld(), "modloader:custom_magic_spell", player.getLocation(), SoundCategory.MASTER, 1.0f, 1.0f);
```

#### Dimension Management

Create, load, and unload custom worlds (dimensions).

```java
// In onLoad(ModAPI api)
DimensionAPI dimensionAPI = api.getDimensionAPI();

// Create a new normal world with default generator
World newWorld = dimensionAPI.createWorld("my_new_dimension", World.Environment.NORMAL);
if (newWorld != null) {
    Bukkit.getLogger().info("Created new dimension: " + newWorld.getName());
}

// Create a new world with a custom chunk generator (MyFlatWorldGenerator would be defined in your mod)
// World flatWorld = dimensionAPI.createWorld("my_flat_world", World.Environment.NORMAL, new MyFlatWorldGenerator());

// Load an existing world
World loadedWorld = dimensionAPI.loadWorld("world_nether");
if (loadedWorld != null) {
    Bukkit.getLogger().info("Loaded existing world: " + loadedWorld.getName());
}

// Unload a world (and save its changes)
// boolean unloaded = dimensionAPI.unloadWorld("my_new_dimension", true);
```

#### Custom Structures

Load and spawn custom structures (e.g., from `.nbt` files) into your worlds.

```java
// In onLoad(ModAPI api)
CustomStructureAPI structureAPI = api.getCustomStructureAPI();

// 1. Load a structure from a file (e.g., an .nbt file in your mod's resources)
// You would typically extract this file from your JAR into the plugin's data folder first.
File structureFile = new File(JavaPlugin.getProvidingPlugin(getClass()).getDataFolder(), "structures/my_house.nbt");
// Ensure structureFile exists and contains valid NBT data

boolean loaded = structureAPI.loadStructure("my_house_structure", structureFile);
if (loaded) {
    Bukkit.getLogger().info("Loaded custom structure: my_house_structure");
}

// 2. Spawn the loaded structure at a location
// Location spawnLoc = new Location(Bukkit.getWorld("world"), 100, 64, 100);
// Random random = new Random();
// boolean spawned = structureAPI.spawnStructure("my_house_structure", spawnLoc, random, 0, false, 1.0f);
// if (spawned) {
//     Bukkit.getLogger().info("Spawned my_house_structure at " + spawnLoc.toVector());
// }
```

### Building Your Mod

1.  **Package:** Run `mvn package` or `gradle build` in your mod's project directory.
2.  **Rename:** Locate the generated `.jar` file in your `target/` (Maven) or `build/libs/` (Gradle) folder. Rename its extension from `.jar` to `.modloader999` (e.g., `MyAwesomeMod-1.0.0.jar` becomes `MyAwesomeMod-1.0.0.modloader999`).
3.  **Deploy:** Place the `.modloader999` file into the `plugins/ModLoader999/Mods/` folder on your Minecraft server.
4.  **Reload:** Restart your server or use `/modloader reload` to load your new mod.
