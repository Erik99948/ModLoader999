# ModLoader999

ModLoader999 is a powerful PaperMC plugin designed to revolutionize how custom content is added to your Minecraft server. It acts as a universal loader, allowing server owners to easily integrate custom items, mobs, blocks, and more by simply dropping mod files into a folder.

## For Server Owners

### Installation

1.  Download the latest `ModLoader999-x.x.x.jar` from the releases page.
2.  Place the jar file into your server's `plugins` folder.
3.  Restart your server. The plugin will generate its necessary folders.
4.  Place your desired `.modloader999` mod files into the `plugins/ModLoader999/Mods/` directory.
5.  Restart your server or use `/modloader reload`.

## For Mod Developers

This guide will walk you through creating your own mods for ModLoader999.

### Project Setup

You can use either Maven or Gradle to set up your mod project.

#### Maven

1.  **Add the Repository:** Add the following repository to your `pom.xml` to access the ModLoader999 API.

    ```xml
    <repositories>
        <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/erik99948/modloader999</url>
        </repository>
        <repository>
            <id>papermc</id>
            <url>https://repo.papermc.io/repository/maven-public/</url>
        </repository>
    </repositories>
    ```

2.  **Add the Dependency:** Add the ModLoader999 API and Paper API as dependencies.

    ```xml
    <dependencies>
        <dependency>
            <groupId>io.github.erik99948</groupId>
            <artifactId>modloader</artifactId>
            <version>1.0.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>io.papermc.paper</groupId>
            <artifactId>paper-api</artifactId>
            <version>1.21-R0.1-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
    ```

#### Gradle

1.  **Add the Repository:** Add the following to your `build.gradle` file.

    ```gradle
    repositories {
        mavenCentral()
        maven { url 'https://repo.papermc.io/repository/maven-public/' }
        maven { url 'https://maven.pkg.github.com/erik99948/modloader999' }
    }
    ```

2.  **Add the Dependency:**

    ```gradle
    dependencies {
        compileOnly 'io.github.erik99948:modloader:1.0.0'
        compileOnly 'io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT'
    }
    ```

### Creating Your First Mod

1.  **`modinfo.json`:** Create a `modinfo.json` file in your `src/main/resources` directory. This file tells ModLoader999 about your mod.

    ```json
    {
      "main": "com.yourname.mod.MyMod",
      "name": "MyFirstMod",
      "version": "1.0.0",
      "author": "YourName"
    }
    ```

2.  **`ModInitializer`:** Create a main class that implements the `ModInitializer` interface.

    ```java
    package com.yourname.mod;

    import com.example.modloader.api.ModAPI;
    import com.example.modloader.api.ModInitializer;

    public class MyMod implements ModInitializer {
        @Override
        public void onLoad(ModAPI api) {
            // Register all your content here!
            System.out.println("My First Mod has been loaded!");
        }
    }
    ```

### API Usage & Examples

You will use the `ModAPI` object passed to your `onLoad` method to register all your custom content.

#### Custom Items

To create a custom item, you create a standard `ItemStack` and register it with a unique ID.

```java
// In your onLoad(ModAPI api) method:
ItemStack myItem = new ItemStack(Material.DIAMOND_HOE);
ItemMeta meta = myItem.getItemMeta();
meta.setDisplayName("§5Magic Hoe");
meta.setLore(Arrays.asList("This hoe has magical properties."));
meta.setCustomModelData(12345); // For your resource pack
myItem.setItemMeta(meta);

api.registerItem("magic_hoe", myItem);
```

#### Custom Blocks

You can create custom blocks with specific behaviors and drops.

```java
// In your onLoad(ModAPI api) method:
CustomBlock myBlock = new CustomBlock(
    "magic_ore",
    Material.COAL_ORE,
    54321, // CustomModelData
    "§dMagic Ore",
    Arrays.asList("This ore glows faintly."),
    null, // Optional: BlockPlaceBehavior
    null, // Optional: BlockBreakBehavior
    null, // Optional: BlockInteractBehavior
    Arrays.asList(new ItemStack(Material.DIAMOND, 2)) // Custom Drops
);

api.registerBlock(myBlock);
```

#### Custom Mobs & Natural Spawning

Define custom mobs with unique stats and AI goals. You can also make them spawn naturally.

```java
// In your onLoad(ModAPI api) method:

// 1. Define the Mob
CustomMob myMob = new CustomMob(
    "goblin",
    EntityType.ZOMBIE,
    "§aGoblin",
    30.0, // Health
    5.0,  // Attack
    0.3,  // Speed
    11111, // CustomModelData
    null // Optional: List<CustomMobGoal>
);
api.registerMob(myMob);

// 2. Define a Spawner for natural spawning
api.registerMobSpawner(new CustomMobSpawner() {
    @Override
    public List<LivingEntity> spawn(World world, Random random, Block block) {
        // Spawn a goblin on grass blocks at night, 1% chance
        if (block.getType() == Material.GRASS_BLOCK && world.getTime() > 13000 && random.nextInt(100) == 0) {
            Location spawnLocation = block.getLocation().add(0.5, 1, 0.5);
            Entity spawned = api.getCustomMobRegistry().spawn("goblin", spawnLocation);
            if (spawned != null) {
                return Arrays.asList((LivingEntity) spawned);
            }
        }
        return Collections.emptyList();
    }
});
```

#### Custom World Generation

You can add custom ores, trees, and other features to world generation.

**Custom Ore Generation:**

```java
// In your onLoad(ModAPI api) method:
api.registerOreGenerator(new CustomOreGenerator() {
    @Override
    public void generate(World world, Random random, int chunkX, int chunkZ) {
        // 10% chance to generate a vein in a chunk
        if (random.nextInt(10) == 0) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = random.nextInt(40) + 10; // Between y=10 and y=50
            
            // Note: You need to register "magic_ore" as a custom block first!
            // This example just places a vanilla block.
            world.getBlockAt(x, y, z).setType(Material.DIAMOND_ORE);
        }
    }
});
```

**Custom Populators (for general features):**

```java
// In your onLoad(ModAPI api) method:
api.registerWorldPopulator(new CustomWorldPopulator() {
    @Override
    public void populate(World world, Random random, Chunk chunk) {
        // 5% chance to place a gold block on the surface of a chunk
        if (random.nextInt(20) == 0) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int y = world.getHighestBlockYAt(chunk.getX() * 16 + x, chunk.getZ() * 16 + z);
            chunk.getBlock(x, y, z).setType(Material.GOLD_BLOCK);
        }
    }
});
```

#### Other Features

The API also supports registering commands, event listeners, and recipes.

**Custom Commands:**
```java
api.registerCommand("mymod", new ModCommandExecutor() {
    @Override
    public boolean onCommand(CommandSender sender, String commandLabel, String[] args) {
        sender.sendMessage("My mod is working!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
        return null; // No tab completion
    }
});
```

**Custom Event Listeners:**
```java
api.registerListener(new Listener() {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome! This server uses MyFirstMod!");
    }
});
```

**Custom Recipes:**
```java
NamespacedKey key = new NamespacedKey(myPluginInstance, "magic_hoe_recipe");
ShapedRecipe recipe = new ShapedRecipe(key, myMagicHoeItemStack); // Use the item you registered
recipe.shape("DD ", " S ", " S ");
recipe.setIngredient('D', Material.DIAMOND);
recipe.setIngredient('S', Material.STICK);
api.registerRecipe(recipe);
```

### Building Your Mod

1.  Run `mvn package` or `gradle build`.
2.  Find the output `.jar` file in your `target` or `build/libs` folder.
3.  Rename the file to have a `.modloader999` extension (e.g., `MyFirstMod-1.0.0.jar` -> `MyFirstMod.modloader999`).
4.  Drop it in the server's `plugins/ModLoader999/Mods/` folder.

---

