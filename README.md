# ModLoader999

Welcome to the official documentation for ModLoader999, a powerful PaperMC plugin that allows you to create and load custom content into your Minecraft server with ease.

## Table of Contents

*   [For Server Owners](#for-server-owners)
    *   [Installation](#installation)
    *   [Security and Permissions (`mod.policy`)](#security-and-permissions-modpolicy)
    *   [Web-Based Management Dashboard](#web-based-management-dashboard)
*   [For Mod Developers](#for-mod-developers)
    *   [Project Setup](#project-setup)
        *   [Maven](#maven)
        *   [Gradle](#gradle)
    *   [The `modinfo.json` File](#the-modinfojson-file)
    *   [Dependency Injection](#dependency-injection)
        *   [Intra-mod Dependencies](#intra-mod-dependencies)
        *   [Inter-mod Dependencies and API Providers/Consumers](#inter-mod-dependencies-and-api-providersconsumers)
    *   [The `ModInitializer` Class](#the-modinitializer-class)
    *   [Registering Content: A Deep Dive](#registering-content-a-deep-dive)
        *   [Custom Items](#custom-items)
        *   [Custom Blocks](#custom-blocks)
        *   [Custom Mobs](#custom-mobs)
        *   [Custom Commands](#custom-commands)
        *   [Event Listeners](#event-listeners)
        *   [Custom Recipes](#custom-recipes)
        *   [Custom Enchantments](#custom-enchantments)
        *   [Custom Potion Effects](#custom-potion-effects)
        *   [World Generation](#world-generation)
        *   [Custom Inventories (GUIs)](#custom-inventories-guis)
        *   [Custom Particles](#custom-particles)
        *   [Custom Sounds](#custom-sounds)
        *   [Dimension Management](#dimension-management)
        *   [Custom Structures](#custom-structures)
    *   [Mod Configuration](#mod-configuration)
    *   [Building Your Mod](#building-your-mod)
    *   [Hot-Reloading for Faster Development](#hot-reloading-for-faster-development)


## For Server Owners

### Installation

1.  **Download:** Obtain the latest `ModLoader999-x.x.x.jar` from the official release page.
2.  **Install:** Place the downloaded `.jar` file into your server's `plugins` folder.
3.  **Restart:** Start or restart your server to generate the necessary configuration files and folders.
4.  **Add Mods:** Place your desired mod files (with a `.modloader999` extension) into the `plugins/ModLoader999/Mods/` directory.
5.  **Manage Mods:** Utilize the following in-game commands, requiring appropriate permissions:
    *   `/modloader list`: Displays all loaded and available mods along with their current status.
    *   `/modloader info <modName>`: Provides detailed information about a specific mod, including its dependencies and current state.
    *   `/modloader reload`: Initiates a full reload of all mods, disabling and then re-enabling them based on their defined dependencies.
    *   `/modloader enable <modName>`: Activates a previously disabled or unloaded mod, automatically resolving its dependencies.
    *   `/modloader disable <modName>`: Deactivates an enabled mod. Note that dependent mods will prevent a mod from being disabled.
    *   `/modloader load <modName>`: Loads a mod that was previously unloaded from memory.
    *   `/modloader hotreload <modName>`: Performs a hot-reload of a single mod, involving its temporary disabling, unloading, reloading, and re-enabling for rapid development iteration.
    *   `/modloader gui`: Opens an interactive in-game graphical user interface for comprehensive mod management.
    *   `/modloader unload <modName>`: Completely unloads a mod from server memory. Dependent mods will prevent a mod from being unloaded.

### Security and Permissions (`mod.policy`)

ModLoader999 incorporates a robust security system leveraging Java's Security Manager. This system is enforced by the `mod.policy` file, located in your `plugins/ModLoader999/` directory. This file meticulously defines the permissions granted to each mod, thereby restricting their access to sensitive server resources such as file systems, network connections, and system properties.

**Understanding `mod.policy`:**

The `mod.policy` file adheres to a standard Java policy file syntax. By default, mods are granted a limited set of permissions to ensure optimal server stability and security. You possess the flexibility to customize this file, allowing you to grant additional permissions to trusted mods or impose further restrictions as deemed necessary.

**Example Customization:**

To grant a specific mod, identified as `MyTrustedMod`, read access to an external directory located at `/path/to/external/data`, you would append a `grant` entry similar to the following:

```policy
grant codeBase "file:${user.dir}/plugins/ModLoader999/Mods/MyTrustedMod-1.0.0.modloader999" {
    permission java.io.FilePermission "/path/to/external/data/-", "read";
};
```

To allow a mod to make outgoing HTTP connections to `example.com`, you would add:

```policy
grant codeBase "file:${user.dir}/plugins/ModLoader999/Mods/MyTrustedMod-1.0.0.modloader999" {
    permission java.net.SocketPermission "example.com:80", "connect";
};
```

**Important Considerations:**

*   **Caution:** Incorrect modifications to the `mod.policy` file can severely compromise your server's security posture. It is imperative to grant only the absolutely necessary permissions and exclusively to mods that are fully trusted.
*   **Mod-Specific Permissions:** Permissions are typically assigned based on the mod's JAR file location, specified by `codeBase`. Ensure that the `codeBase` path accurately reflects the deployment location of your mod.
*   **Reloading:** Any alterations made to the `mod.policy` file necessitate a full server restart for the changes to be effectively applied.

### Web-Based Management Dashboard

ModLoader999 integrates a lightweight yet comprehensive web server that hosts a management dashboard. This dashboard empowers server owners to monitor mod status, control mod activation and deactivation, configure mod settings, and perform various other management tasks directly from a web browser.

**Accessing the Dashboard:**

The dashboard is readily accessible via your server's IP address combined with the configured web server port, which defaults to `25566`. For instance, if your server's IP address is `192.168.1.100`, you would navigate to the dashboard by entering `http://192.168.1.100:25566/` into your web browser.

**Features:**

*   **Mod Listing:** View an exhaustive list of all installed mods, complete with their versions, authors, descriptive summaries, and current operational status.
*   **Mod Toggling:** Effortlessly enable or disable mods with a single, intuitive click.
*   **Mod Hot-Reloading:** Initiate a hot-reload for individual mods, facilitating rapid testing and deployment during development cycles.
*   **Configuration Management:** Access and modify mod configuration files directly through the web interface, providing a convenient way to fine-tune mod behavior.

## For Mod Developers

This comprehensive guide provides an in-depth overview of the process involved in creating custom mods utilizing the ModLoader999 API.

### Project Setup

To commence mod development, it is essential to establish a Java project using either Maven or Gradle as your build automation tool.

#### Maven

1.  **Add Repositories:** Incorporate the PaperMC and ModLoader999 repositories into your `pom.xml` file. This ensures that your project can locate and download the necessary dependencies.

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

2.  **Add Dependencies:** Declare the Paper API and ModLoader999 API as dependencies within your `pom.xml`. The `provided` scope indicates that these dependencies will be supplied by the server runtime and should not be bundled with your mod.

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
            <version>3.0.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
    ```

#### Gradle

1.  **Add Repositories:** Include the following repository declarations in your `build.gradle` file. These repositories are crucial for resolving project dependencies.

    ```gradle
    repositories {
        mavenCentral()
        maven { url 'https://repo.papermc.io/repository/maven-public/' }
        maven { url 'https://maven.pkg.github.com/erik99948/modloader999' }
    }
    ```

2.  **Add Dependencies:** Specify the Paper API and ModLoader999 API as `compileOnly` dependencies. This ensures that the APIs are available during compilation but are not bundled into your final mod JAR, as they are provided by the server environment.

    ```gradle
    dependencies {
        compileOnly 'io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT'
        compileOnly 'io.github.erik99948:modloader:3.0.0'
    }
    ```

### The `modinfo.json` File

Every mod developed for ModLoader999 must contain a `modinfo.json` file, strategically placed within the `src/main/resources` directory of your project. This JSON file serves as a critical manifest, providing essential metadata that ModLoader999 utilizes to identify, load, and manage your mod.

```json
{
  "id": "myfirstmod",
  "main": "com.yourname.mod.MyMod",
  "name": "MyFirstMod",
  "version": "1.0.0",
  "author": "YourName",
  "description": "A brief description of what your mod does.",
  "apiVersion": "1.0.0",
  "dependencies": {
    "AnotherMod": "^1.2.0"
  },
  "softDependencies": [
    "OptionalMod"
  ],
  "customProperties": {
    "setting1": "value1",
    "setting2": "value2"
  }
}
```

*   `id`: A unique identifier for your mod, typically in lowercase and without spaces (e.g., `myfirstmod`).
*   `main`: The fully qualified class name of your mod's primary entry point, which must implement the `ModInitializer` interface (e.g., `com.yourname.mod.MyMod`).
*   `name`: The human-readable name of your mod (e.g., `MyFirstMod`).
*   `version`: The current version of your mod. It is highly recommended to adhere to [Semantic Versioning (SemVer)](https://semver.org/) principles (e.g., `1.0.0`).
*   `author`: The name of the mod developer or team.
*   `description`: A concise summary of your mod's functionality and purpose.
*   `apiVersion`: The specific version of the ModLoader999 API that your mod is built against. This crucial field ensures compatibility and prevents your mod from loading with incompatible ModLoader999 versions.
*   `dependencies` (Optional): A JSON object listing other mods that your mod strictly requires to function correctly. The keys are the mod IDs, and the values are the required [Semantic Versioning ranges](https://semver.org/) (e.g., `"AnotherMod": "^1.2.0"`). If any hard dependency is not met, your mod will not be loaded.
*   `softDependencies` (Optional): A JSON array of mod IDs that your mod can optionally depend on. If these mods are present on the server, ModLoader999 will ensure they are loaded before your mod. However, if they are absent, your mod will still load without error, allowing for enhanced functionality when optional dependencies are met.
*   `customProperties` (Optional): A JSON object allowing you to define any custom key-value pairs specific to your mod. These properties can be accessed programmatically and are also exposed via the web dashboard for configuration.

### Dependency Injection

ModLoader999 employs a sophisticated dependency injection (DI) system to streamline the management of dependencies both within your mod and between different mods. This system empowers you to effortlessly inject your own classes and leverage APIs exposed by other mods.

#### Intra-mod Dependencies

To manage dependencies exclusively within your own mod, you must first bind the desired class or interface within the `configure` method of your `ModInitializer`. Subsequently, you can utilize constructor injection to automatically receive an instance of that bound dependency. The DI system also supports advanced features such as `@Provides` methods for complex object creation logic, `@Named` annotations for disambiguating multiple implementations of the same interface, and `@Singleton` to control the lifecycle of your objects, ensuring only a single instance exists throughout the application.

**Greeter.java:**
```java
package com.example.mod;

public class Greeter {
    public String getGreeting() {
        return "Hello from the Greeter class!";
    }
}
```

**MyMod.java:**
```java
package com.example.mod;

import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModInitializer;
import com.example.modloader.api.dependencyinjection.Binder;
import com.example.modloader.api.dependencyinjection.Provides;
import com.example.modloader.api.dependencyinjection.Singleton;

public class MyMod implements ModInitializer {

    private final Greeter greeter;

    public MyMod(Greeter greeter) {
        this.greeter = greeter;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(Greeter.class, new Greeter());
    }

    @Provides
    @Singleton
    public AnotherService provideAnotherService() {
        return new AnotherService();
    }

    @Override
    public void onLoad(ModAPI api) {
        System.out.println(greeter.getGreeting());
    }
}
```

**AnotherService.java:**
```java
package com.example.mod;

public class AnotherService {
    public String getServiceMessage() {
        return "Message from another service!";
    }
}
```

#### Inter-mod Dependencies and API Providers/Consumers

To seamlessly integrate and utilize an API provided by another mod, you must first declare this dependency within your `modinfo.json` file. Following this declaration, you can effortlessly inject the API class directly into your mod's constructor. The providing mod is responsible for exposing its API by annotating the API interface or class with `@API`. ModLoader999's advanced dependency injection system now fully supports scenarios where multiple mods might provide different implementations of the same API. In such cases, you have the flexibility to inject a `List<YourAPIInterface>` into your constructor to receive all currently registered implementations of that API.

**CoolMod's `CoolAPI.java`:**
```java
package com.example.coolmod;

import com.example.modloader.api.dependencyinjection.API;

@API
public class CoolAPI {
    public String getCoolMessage() {
        return "This is a cool message from CoolMod!";
    }
}
```

**AwesomeMod's `modinfo.json`:**
```json
{
  "main": "com.example.awesomemod.AwesomeMod",
  "name": "AwesomeMod",
  "version": "1.0.0",
  "author": "YourName",
  "dependencies": {
    "CoolMod": "^1.0.0"
  }
}
```

**AwesomeMod's `AwesomeMod.java`:**
```java
package com.example.awesomemod;

import com.example.coolmod.CoolAPI;
import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModInitializer;
import com.example.modloader.api.dependencyinjection.Binder;
import java.util.List;

public class AwesomeMod implements ModInitializer {

    private final CoolAPI coolAPI;
    private final List<CoolAPI> allCoolAPIs;

    public AwesomeMod(CoolAPI coolAPI, List<CoolAPI> allCoolAPIs) {
        this.coolAPI = coolAPI;
        this.allCoolAPIs = allCoolAPIs;
    }

    @Override
    public void configure(Binder binder) {
    }

    @Override
    public void onLoad(ModAPI api) {
        System.out.println(coolAPI.getCoolMessage());
        allCoolAPIs.forEach(apiInstance -> System.out.println("All Cool APIs: " + apiInstance.getCoolMessage()));
    }
}
```

### The `ModInitializer` Class

Your mod's primary entry point must be a class that implements the `ModInitializer` interface. This interface mandates several methods that ModLoader999 invokes at distinct phases of your mod's lifecycle. The `onLoad` method serves as the central entry point for your mod, where you will typically register all of your custom content. The `ModAPI` instance provided to these lifecycle methods is uniquely scoped to your mod, ensuring that all registrations (such as commands, event listeners, etc.) are automatically associated with and managed by your mod's lifecycle.

**Event System with `@SubscribeEvent`:**

ModLoader999's event system now leverages the `@SubscribeEvent` annotation for a more intuitive and flexible way to handle events. You can create any plain Java class, annotate its methods with `@SubscribeEvent`, and then register an instance of this class using `modAPI.registerEventListener()`. The event system will automatically discover and dispatch events to these annotated methods.

Event priorities (`LOWEST`, `LOW`, `NORMAL`, `HIGH`, `HIGHEST`, `MONITOR`) can be specified within the `@SubscribeEvent` annotation to control the order of execution. Many 'Pre' events (e.g., `ModPreLoadEvent`, `PreRegisterBlockEvent`, `PreRegisterItemEvent`) are cancellable, allowing your mod to intercept and prevent actions before they occur.

**Example of using `@SubscribeEvent`:**

This example demonstrates how to listen for the `ModPreLoadEvent` and `PreRegisterBlockEvent` using the new `@SubscribeEvent` annotation.

```java
package com.yourname.mod;

import com.example.modloader.api.event.ModPreLoadEvent;
import com.example.modloader.api.event.PreRegisterBlockEvent;
import com.example.modloader.api.event.SubscribeEvent;
import com.example.modloader.api.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class MyModEventListener {

    private final Logger logger;

    public MyModEventListener(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onModPreLoad(ModPreLoadEvent event) {
        logger.info("MyModEventListener: Mod " + event.getModInfo().getName() + " is pre-loading!");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreRegisterBlock(PreRegisterBlockEvent event) {
        if (event.getBlock().getId().equals("forbidden_block")) {
            event.setCancelled(true);
            logger.info("MyModEventListener: Prevented registration of forbidden_block!");
        }
    }
}
```

**Registering the Event Listener:**

```java
// In your ModInitializer's onPreLoad(ModAPI api) method
api.registerEventListener(new MyModEventListener(api.getPlugin()));
```

The lifecycle methods are:
- `configure(Binder binder)`: This method is invoked first during your mod's loading sequence. It is the designated place for you to configure your mod's dependency injection bindings.
- `onPreLoad(ModAPI api)`: Executed just before the main `onLoad` method. This phase is suitable for preliminary setup that doesn't require full API access.
- `onLoad(ModAPI api)`: This is the core entry point for your mod. All primary content registration logic, such as items, blocks, and commands, should be implemented here.
- `onPostLoad(ModAPI api)`: Called immediately after the `onLoad` method has successfully completed. This phase is ideal for post-load adjustments or inter-mod communication that relies on other mods being fully loaded.
- `onEnable()`: Invoked when your mod is enabled. This typically occurs after all mods have been loaded and initialized.
- `onPreDisable()`: Executed just before your mod is disabled. This is a suitable phase for any pre-disabling cleanup or state saving.
- `onDisable()`: Called when your mod is disabled. Implement your primary cleanup and resource release logic here.
- `onPostDisable()`: Invoked after your mod has been fully disabled. This is the final cleanup phase.

```java
package com.yourname.mod;

import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModInitializer;
import com.example.modloader.api.dependencyinjection.Binder;

public class MyMod implements ModInitializer {

    @Override
    public void configure(Binder binder) {
    }

    @Override
    public void onPreLoad(ModAPI api) {
        System.out.println("My First Mod is about to load!");
    }

    @Override
    public void onLoad(ModAPI api) {
        System.out.println("My First Mod is loading!");
    }

    @Override
    public void onPostLoad(ModAPI api) {
        System.out.println("My First Mod has loaded!");
    }

    @Override
    public void onEnable() {
        System.out.println("My First Mod is enabling!");
    }

    @Override
    public void onPreDisable() {
        System.out.println("My First Mod is about to disable!");
    }

    @Override
    public void onDisable() {
        System.out.println("My First Mod is disabling!");
    }

    @Override
    public void onPostDisable() {
        System.out.println("My First Mod has disabled!");
    }
}
```

### Registering Content: A Deep Dive

The `ModAPI` object, meticulously provided in the `onLoad` method, serves as your comprehensive gateway to all of ModLoader999's powerful features and functionalities.

#### Custom Items

Custom items are seamlessly integrated by utilizing standard Bukkit `ItemStack` objects. The pivotal aspect lies in employing `CustomModelData` to establish a definitive link between your custom item and its corresponding custom texture within your resource pack. This ensures that your custom items are visually represented as intended within the game.

```java
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

// In your ModInitializer's onLoad(ModAPI api) method
ItemStack myItem = new ItemStack(Material.DIAMOND_SWORD);
ItemMeta meta = myItem.getItemMeta();
meta.setDisplayName("§6Legendary Sword");
meta.setLore(Arrays.asList("A sword of great power.", "Forged in the fires of Mount Cinder."));
meta.setCustomModelData(1337);
myItem.setItemMeta(meta);

api.registerItem("legendary_sword", myItem);
```

#### Custom Blocks

Custom blocks are meticulously defined through a `CustomBlock` object. This object precisely specifies the base material, `CustomModelData` for resource pack integration, and a suite of optional behaviors. These behaviors empower you to define intricate custom logic that dictates how your block responds to various in-game events, including placement, destruction, player interaction, redstone power fluctuations, and explosive forces.

```java
import com.example.modloader.CustomBlock;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;
import java.util.List;

// In your ModInitializer's onLoad(ModAPI api) method
CustomBlock myBlock = new CustomBlock(
    "magic_crystal",
    Material.GLASS,
    1338,
    "§bMagic Crystal",
    Arrays.asList("A crystal that hums with energy.", "Emits a soft, ethereal glow."),
    (event, block, player) -> {
        player.sendMessage("You have placed a shimmering magic crystal!");
        // Additional custom logic for block placement
    },
    (event, block, player) -> {
        player.sendMessage("The magic crystal shatters with a faint chime!");
        // Custom break logic, such as dropping unique items or triggering effects
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.DIAMOND));
    },
    (event, block, player) -> {
        player.sendMessage("You feel a strange energy emanating from the crystal.");
        // Custom interaction logic, like opening a GUI or applying a potion effect
    },
    (event, block, oldCurrent, newCurrent) -> {
        if (newCurrent > 0 && oldCurrent == 0) {
            block.getWorld().strikeLightningEffect(block.getLocation());
            block.getWorld().createExplosion(block.getLocation(), 1.0f, false, false);
        }
    },
    (event, block, blockList, yield) -> {
        block.getWorld().createExplosion(block.getLocation(), 2.0f, false, false);
        event.setCancelled(true);
    },
    Arrays.asList(new ItemStack(Material.DIAMOND, 2)) // Custom drops when broken
);
api.registerBlock(myBlock);
```

#### Custom Mobs

Custom mobs are built upon existing Minecraft entities, allowing for extensive customization of their statistics, visual appearance, and behavioral patterns through the implementation of `CustomMobGoal`s. Furthermore, you can define custom spawners to control their natural generation within the game world.

```java
import com.example.modloader.CustomMob;
import com.example.modloader.api.mob.CustomMobGoal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// In your ModInitializer's onLoad(ModAPI api) method

// 1. Define Custom Mob Goals (optional) - These define the mob's AI behavior.
List<CustomMobGoal> goals = Arrays.asList(
    new CustomMobGoal() {
        @Override
        public boolean shouldStart(LivingEntity entity) {
            return entity.getTarget() != null && entity.getTarget().getLocation().distance(entity.getLocation()) > 3;
        }

        @Override
        public void start(LivingEntity entity) {
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 1.0f);
            entity.sendMessage("I see you!");
        }

        @Override
        public void stop(LivingEntity entity) {
            entity.sendMessage("Lost target.");
        }

        @Override
        public void tick(LivingEntity entity) {
            entity.getNavigation().moveTo(entity.getTarget(), 1.2);
        }
    }
);

// 2. Define the Custom Mob - This sets up the mob's core properties.
CustomMob myMob = new CustomMob(
    "goblin_warrior",
    EntityType.ZOMBIE,
    "§aGoblin Warrior",
    25.0,
    4.0,
    0.35,
    1339,
    goals,
    Map.of(Attribute.GENERIC_ARMOR, 5.0, Attribute.GENERIC_MOVEMENT_SPEED, 0.4)
);
api.registerMob(myMob);

// 3. Register a Custom Mob Spawner (optional) - This controls where and how your mob naturally generates.
api.registerMobSpawner((world, random, block) -> {
    if (block.getBiome() == org.bukkit.block.Biome.FOREST && random.nextInt(100) < 5) {
        LivingEntity spawnedMob = (LivingEntity) api.getCustomMobRegistry().spawn("goblin_warrior", block.getLocation());
        if (spawnedMob != null) {
            // Example: Make the spawned mob target the nearest player
            world.getPlayers().stream().min(Comparator.comparingDouble(p -> p.getLocation().distance(spawnedMob.getLocation())))
                 .ifPresent(spawnedMob::setTarget);
        }
    }
    return null;
});
```

#### Custom Commands

Register custom commands with comprehensive tab completion support. Your `ModCommandExecutor` implementation will meticulously handle both the execution logic of the command and provide intelligent tab completion suggestions to players. Commands registered through this API are automatically associated with your mod's lifecycle, ensuring they are properly registered upon mod enablement and gracefully unregistered when your mod is disabled or unloaded.

```java
import org.bukkit.command.CommandSender;
import com.example.modloader.api.ModCommandExecutor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// In your ModInitializer's onLoad(ModAPI api) method
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
        return Collections.emptyList();
    }
});
```

#### Event Listeners

ModLoader999's event system allows you to react to a wide array of in-game events, both vanilla Bukkit events and custom `ModEvent`s. By using the `@SubscribeEvent` annotation on methods within your listener classes, you can easily subscribe to events. These listener classes are then registered with `ModAPI.registerEventListener()`.

```java
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.Material;
import com.example.modloader.api.event.SubscribeEvent;
import com.example.modloader.api.event.EventPriority;
import com.example.modloader.api.event.ModPreLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

// 1. Create your event listener class. It can be a plain Java class.
public class MyModGameListener {

    private final Logger logger;

    public MyModGameListener(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
    }

    // Listen for a vanilla Bukkit event
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("§bWelcome, " + event.getPlayer().getName() + "! This server is running a cool mod!");
    }

    // Listen for another vanilla Bukkit event
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.STONE) {
            event.getPlayer().sendMessage("§7You broke some stone!");
        }
    }

    // Listen for a custom ModEvent
    @SubscribeEvent
    public void onModPreLoad(ModPreLoadEvent event) {
        logger.info("MyModGameListener: Mod " + event.getModInfo().getName() + " is pre-loading!");
    }
}

// 2. Register the listener in your ModInitializer's onLoad(ModAPI api) method
// For vanilla Bukkit Listeners, you can still use registerListener
api.registerListener(new MyModGameListener(api.getPlugin()));

// For custom ModEvents (or a mix of both), use registerEventListener
api.registerEventListener(new MyModGameListener(api.getPlugin()));
```

#### Custom Recipes

Register custom crafting recipes for your unique items, enabling players to craft them within the game. The API supports various recipe types, including `ShapedRecipe` for structured crafting, `ShapelessRecipe` for unordered ingredients, and `FurnaceRecipe` for smelting processes.

```java
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

// In your ModInitializer's onLoad(ModAPI api) method

// Assume 'myItem' is an ItemStack for a custom item previously registered
ItemStack myItem = new ItemStack(Material.DIAMOND_SWORD);

// Shaped Recipe: Craft a legendary sword using diamonds and sticks
NamespacedKey shapedKey = new NamespacedKey(JavaPlugin.getProvidingPlugin(getClass()), "legendary_sword_recipe");
ShapedRecipe shapedRecipe = new ShapedRecipe(shapedKey, myItem);
shapedRecipe.shape(" D ", " D ", " S "); // D = Diamond, S = Stick
shapedRecipe.setIngredient('D', Material.DIAMOND);
shapedRecipe.setIngredient('S', Material.STICK);
api.registerRecipe(shapedRecipe);

// Shapeless Recipe: Craft 9 dirt blocks from 1 grass block
NamespacedKey shapelessKey = new NamespacedKey(JavaPlugin.getProvidingPlugin(getClass()), "dirt_from_grass");
ShapelessRecipe shapelessRecipe = new ShapelessRecipe(shapelessKey, new ItemStack(Material.DIRT, 9));
shapelessRecipe.addIngredient(Material.GRASS_BLOCK);
api.registerRecipe(shapelessRecipe);

// Furnace Recipe: Smelt raw iron into an iron ingot
NamespacedKey furnaceKey = new NamespacedKey(JavaPlugin.getProvidingPlugin(getClass()), "iron_ingot_smelting");
FurnaceRecipe furnaceRecipe = new FurnaceRecipe(furnaceKey, new ItemStack(Material.IRON_INGOT), Material.RAW_IRON, 0.7F, 200);
api.registerRecipe(furnaceRecipe);
```

#### Custom Enchantments

Create and register unique enchantments that extend the gameplay experience. It is important to note that while ModLoader999 handles the registration of the enchantment type, you are responsible for implementing the actual effects and behaviors of the enchantment through custom event listeners.

```java
import com.example.modloader.CustomEnchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.LivingEntity;
import java.util.Comparator;

// 1. Define your CustomEnchantment class
// This class should reside within your mod's package (e.g., com.yourname.mod).
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

// 2. Register the enchantment in your ModInitializer's onLoad(ModAPI api) method
CustomEnchantmentAPI enchantmentAPI = api.getCustomEnchantmentAPI();
enchantmentAPI.registerEnchantment(new LifestealEnchantment(JavaPlugin.getProvidingPlugin(getClass())));

// 3. Implement the enchantment's effects using an Event Listener
api.registerListener(new Listener() {
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LivingEntity && event.getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) event.getDamager();
            ItemStack weapon = attacker.getEquipment().getItemInMainHand();

            LifestealEnchantment lifesteal = (LifestealEnchantment) enchantmentAPI.getEnchantment("lifesteal");
            if (lifesteal != null && weapon.containsEnchantment(lifesteal)) {
                int level = weapon.getEnchantmentLevel(lifesteal);
                double healAmount = event.getDamage() * (0.05 * level);
                attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + healAmount));
                attacker.sendMessage("§aYou lifesteal " + String.format("%.1f", healAmount) + " health!");
            }
        }
    }
});
```

#### Custom Potion Effects

Define and register custom potion effects to introduce novel gameplay mechanics. Similar to enchantments, while ModLoader999 handles the registration of the potion effect type, you are responsible for implementing the actual effects and their associated logic through custom event listeners or scheduled tasks.

```java
import com.example.modloader.CustomPotionEffectType;
import org.bukkit.Color;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;

// 1. Define your CustomPotionEffectType class
// This class should reside within your mod's package (e.g., com.yourname.mod).
public class PhotosynthesisEffect extends CustomPotionEffectType {
    public PhotosynthesisEffect(JavaPlugin plugin) {
        super(plugin, "photosynthesis", "Photosynthesis", Color.fromRGB(100, 200, 50), false, null);
    }
}

// 2. Register the potion effect in your ModInitializer's onLoad(ModAPI api) method
CustomPotionEffectAPI potionAPI = api.getCustomPotionEffectAPI();
potionAPI.registerPotionEffectType(new PhotosynthesisEffect(JavaPlugin.getProvidingPlugin(getClass())));

// 3. Implement the potion effect's logic using a BukkitRunnable for periodic checks
new BukkitRunnable() {
    @Override
    public void run() {
        PhotosynthesisEffect photosynthesis = (PhotosynthesisEffect) potionAPI.getPotionEffectType("photosynthesis");
        if (photosynthesis == null) return;

        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPotionEffect(photosynthesis)) {
                if (player.getWorld().isDayTime() && player.getLocation().getBlock().getLightFromSky() == 15) {
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
                    player.setSaturation(Math.min(20, player.getSaturation() + 1));
                }
            }
        }
    }
}.runTaskTimer(JavaPlugin.getProvidingPlugin(getClass()), 0L, 40L);
```

#### World Generation

ModLoader999 provides an extensive suite of tools to meticulously customize world generation. This includes the ability to define custom ore, tree, and structure generators, as well as general world populators. You can precisely target specific worlds and biomes for your generation logic. Furthermore, the API offers enhanced functionalities for Biome customization, Custom Dimension Providers, and advanced Procedural Generation Utilities.

**Biome API Enhancements:**

Define highly customized biomes with granular control over their visual properties, including sky, fog, water, grass, and foliage colors. You can also specify ambient particles, sounds, temperature, humidity, and precipitation characteristics.

```java
import com.example.modloader.api.world.CustomBiome;
import org.bukkit.block.Biome;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Color;

CustomWorldGeneratorAPI worldGenAPI = api.getCustomWorldGeneratorAPI();

CustomBiome customForestBiome = new CustomBiome() {
    @Override
    public String getId() { return "my_custom_forest"; }
    @Override
    public String getName() { return "Mystic Forest"; }
    @Override
    public Biome getBaseBiome() { return Biome.FOREST; }
    @Override
    public Color getSkyColor() { return Color.fromRGB(100, 150, 200); }
    @Override
    public Color getFogColor() { return Color.fromRGB(150, 150, 150); }
    @Override
    public Color getWaterColor() { return Color.fromRGB(50, 100, 200); }
    @Override
    public Color getWaterFogColor() { return Color.fromRGB(50, 100, 150); }
    @Override
    public Color getGrassColor() { return Color.fromRGB(50, 150, 50); }
    @Override
    public Color getFoliageColor() { return Color.fromRGB(0, 100, 0); }
    @Override
    public Particle getAmbientParticle() { return Particle.CRIT; }
    @Override
    public int getAmbientParticleCount() { return 5; }
    @Override
    public double getAmbientParticleChance() { return 0.1; }
    @Override
    public Sound getAmbientSound() { return Sound.AMBIENT_CAVE; }
    @Override
    public double getAmbientSoundVolume() { return 0.5; }
    @Override
    public double getAmbientSoundPitch() { return 1.2; }
    @Override
    public float getTemperature() { return 0.7f; }
    @Override
    public float getHumidity() { return 0.8f; }
    @Override
    public boolean hasPrecipitation() { return true; }
};
worldGenAPI.registerCustomBiome(customForestBiome.getId(), customForestBiome);
```

**Custom Dimension Providers:**

Implement a more abstract and flexible framework for mods to define entirely new dimensions. This includes granular control over custom physics, sky rendering, fog effects, and other environmental properties.

```java
import com.example.modloader.api.world.CustomDimension;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

DimensionAPI dimensionAPI = api.getDimensionAPI();

// Define a custom dimension with specific properties
CustomDimension voidDimension = new CustomDimension() {
    @Override
    public String getId() { return "my_void_dimension"; }
    @Override
    public String getName() { return "The Void"; }
    @Override
    public World.Environment getEnvironment() { return World.Environment.THE_END; } // Utilizing THE_END environment for void-like characteristics
    @Override
    public long getSeed() { return 12345L; }
    @Override
    public boolean isHardcore() { return false; }
    @Override
    public boolean hasStorm() { return false; }
    @Override
    public boolean isThundering() { return false; }
    @Override
    public long getFullTime() { return 18000L; } // Configures the dimension to always be night
    @Override
    public double getGravityFactor() { return 0.5; } // Sets gravity to half the normal factor
    @Override
    public String getSkyColorHex() { return "#000000"; } // Defines a black sky color
    @Override
    public String getFogColorHex() { return "#111111"; } // Sets a dark fog color
    @Override
    public ChunkGenerator getChunkGenerator() { return new VoidChunkGenerator(); } // Assigns a custom chunk generator for void generation
};
dimensionAPI.registerCustomDimension(voidDimension.getId(), voidDimension);

// To create a world instance using this custom dimension definition:
World newWorldInstance = dimensionAPI.createWorld(voidDimension);
```

**Procedural Generation Utilities:**

Access a comprehensive set of helper classes and methods specifically designed for common procedural generation tasks, including advanced noise generation algorithms.

```java
import com.example.modloader.api.world.ProceduralGenerationAPI;

ProceduralGenerationAPI procGenAPI = api.getProceduralGenerationAPI();

double noiseValue2D = procGenAPI.generatePerlinNoise(10.5, 20.3, 0.1, 4, 2.0, 0.5, 1234L);
double noiseValue3D = procGenAPI.generateSimplexNoise(10.5, 20.3, 30.1, 0.05, 5678L);
System.out.println("Generated 2D Perlin Noise: " + noiseValue2D);
System.out.println("Generated 3D Simplex Noise: " + noiseValue3D);
```

#### Custom Inventories (GUIs)

Construct interactive graphical user interfaces (GUIs) for your players with a highly flexible layout system. This API allows you to define custom components, arrange them precisely using various layout managers, and implement sophisticated event handling for player interactions.

```java
import com.example.modloader.api.gui.GUIAPI;
import com.example.modloader.api.gui.Layout;
import com.example.modloader.api.gui.GridLayout;
import com.example.modloader.api.gui.GUI;
import com.example.modloader.api.gui.SimpleButton;
import com.example.modloader.api.gui.Label;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

GUIAPI guiAPI = api.getGUIAPI();

// 1. Define a layout for your GUI. A GridLayout is suitable for inventory-based GUIs.
Layout gridLayout = new GridLayout(3, 9);

// 2. Create a new GUI instance with a title, size (in slots), and the defined layout.
GUI myGUI = guiAPI.createGUI("My Mod's GUI", 27, gridLayout);

// 3. Create interactive components and add them to the GUI with layout constraints.
// Example: A simple button positioned at row 1, column 4 (the center of the middle row).
ItemStack buttonItem = new ItemStack(Material.EMERALD);
ItemMeta buttonMeta = buttonItem.getItemMeta();
buttonMeta.setDisplayName("§aClick Me!");
buttonItem.setItemMeta(buttonMeta);

SimpleButton clickButton = new SimpleButton(buttonItem, player -> {
    player.sendMessage("§aButton clicked!");
    // Implement custom logic here, such as triggering an event or performing an action.
});
myGUI.addComponent(clickButton, new GridLayout.GridConstraints(1, 4));

// Example: A static label positioned at row 0, column 0.
ItemStack labelItem = new ItemStack(Material.PAPER);
ItemMeta labelMeta = labelItem.getItemMeta();
labelMeta.setDisplayName("§fWelcome!");
labelItem.setItemMeta(labelMeta);
Label welcomeLabel = new Label(labelItem);
myGUI.addComponent(welcomeLabel, new GridLayout.GridConstraints(0, 0));

// To open this GUI for a specific player, typically in response to a command or event:
// Player targetPlayer = (Player) sender; // Assuming 'sender' is a Player
// guiAPI.openGUI(targetPlayer, myGUI);

// Dynamic GUI Updates:
// Components within the GUI can be updated dynamically after creation.
// For example, to change the text of a label:
// welcomeLabel.setItemStack(new ItemStack(Material.BOOK)); // Update the underlying ItemStack
// myGUI.updateComponent(welcomeLabel, new GridLayout.GridConstraints(0, 0)); // Re-add to refresh the display

// To remove a component from the GUI:
// myGUI.removeComponent(clickButton);
```

#### Custom Particles

Spawn a diverse range of particle effects within the game world to enhance visual feedback or create immersive environmental effects.

```java
import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Color;
import com.example.modloader.api.CustomParticleAPI;

CustomParticleAPI particleAPI = api.getCustomParticleAPI();

// Example: Spawn 10 redstone particles at a player's location with a specific color.
Player player = Bukkit.getOnlinePlayers().iterator().next(); // Get an online player for demonstration
if (player != null) {
    Location particleLocation = player.getLocation();
    particleAPI.spawnParticle(particleLocation.getWorld(), Particle.REDSTONE, particleLocation, 10, 0.5, 0.5, 0.5, 0.1, new Particle.DustOptions(Color.RED, 1.0f));
}

// Example: Spawn a burst of flame particles at a predefined location.
World world = Bukkit.getWorld("world"); // Assuming a world named "world" exists
if (world != null) {
    Location burstLocation = new Location(world, 100, 64, 100);
    particleAPI.spawnParticle(burstLocation.getWorld(), Particle.FLAME, burstLocation, 50, 1.0, 1.0, 1.0, 0.05);
}
```

#### Custom Sounds

Play custom sound effects, either those defined within your mod's resource pack or standard vanilla Minecraft sounds, to provide auditory feedback for in-game events.

```java
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.SoundCategory;
import com.example.modloader.api.CustomSoundAPI;

CustomSoundAPI soundAPI = api.getCustomSoundAPI();

// Example: Play a vanilla sound at a specific location.
World world = Bukkit.getWorld("world");
if (world != null) {
    Location soundLocation = new Location(world, 100, 64, 100);
    soundAPI.playSound(world, Sound.ENTITY_PLAYER_LEVELUP, soundLocation, 1.0f, 1.0f);
}

// Example: Play a custom sound defined in your resource pack's sounds.json at a location.
// Ensure 'modloader:custom_magic_spell' is defined in your resource pack.
if (world != null) {
    Location customSoundLocation = new Location(world, 100, 64, 100);
    soundAPI.playCustomSound(world, "modloader:custom_magic_spell", customSoundLocation, SoundCategory.MASTER, 1.0f, 1.0f);
}
```

#### Dimension Management

Gain comprehensive control over world management by creating, loading, and unloading custom worlds, effectively defining new dimensions within your server environment.

```java
import org.bukkit.World;
import org.bukkit.World.Environment;
import com.example.modloader.api.DimensionAPI;
import org.bukkit.Bukkit;

DimensionAPI dimensionAPI = api.getDimensionAPI();

// Create a new normal world with the default chunk generator.
World newWorld = dimensionAPI.createWorld("my_new_dimension", World.Environment.NORMAL);
if (newWorld != null) {
    Bukkit.getLogger().info("Successfully created new dimension: " + newWorld.getName());
}

// Load an existing world by its name.
World loadedWorld = dimensionAPI.loadWorld("world_nether");
if (loadedWorld != null) {
    Bukkit.getLogger().info("Successfully loaded existing world: " + loadedWorld.getName());
}

// Unload a world, with an option to save its changes.
// boolean unloaded = dimensionAPI.unloadWorld("my_new_dimension", true);
// if (unloaded) {
//     Bukkit.getLogger().info("Successfully unloaded dimension: my_new_dimension");
// }
```

#### Custom Structures

Facilitate the loading and spawning of custom structures, typically defined in `.nbt` files, into your game worlds. This allows for the creation of unique architectural elements or predefined landscapes.

```java
import com.example.modloader.api.CustomStructureAPI;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import java.util.Random;

CustomStructureAPI structureAPI = api.getCustomStructureAPI();

// 1. Load a structure from a file. The .nbt file should be extracted from your mod's JAR
// into the plugin's data folder for accessibility.
File structureFile = new File(JavaPlugin.getProvidingPlugin(getClass()).getDataFolder(), "structures/my_house.nbt");
// Ensure 'structureFile' exists and contains valid NBT data for a Minecraft structure.

boolean loaded = structureAPI.loadStructure("my_house_structure", structureFile);
if (loaded) {
    Bukkit.getLogger().info("Successfully loaded custom structure: my_house_structure");
}

// 2. Spawn the previously loaded structure at a specified location.
World world = Bukkit.getWorld("world");
if (world != null) {
    Location spawnLoc = new Location(world, 100, 64, 100);
    Random random = new Random();
    boolean spawned = structureAPI.spawnStructure("my_house_structure", spawnLoc, random, 0, false, 1.0f);
    if (spawned) {
        Bukkit.getLogger().info("Spawned my_house_structure at " + spawnLoc.toVector());
    }
}
```

#### Voice Chat API

ModLoader999 now provides a comprehensive Voice Chat API, enabling mod developers to implement real-time voice communication within their mods. This API abstracts away the complexities of audio capture, playback, and network transmission, allowing you to focus on game-specific logic like proximity chat, voice channels, or custom audio effects.

**Accessing the VoiceAPI:**

You can obtain an instance of the `VoiceAPI` through the main `ModAPI` object provided to your `ModInitializer`'s lifecycle methods.

```java
import com.example.modloader.api.VoiceAPI;

// In your ModInitializer's onLoad(ModAPI api) method
VoiceAPI voiceAPI = api.getVoiceAPI();
```

**Core Functionalities:**

The `VoiceAPI` offers the following methods:

*   `startVoiceCapture()`: Initiates audio capture from the player's default microphone. Once started, the API will continuously capture audio.
*   `stopVoiceCapture()`: Halts the audio capture process.
*   `sendVoiceData(byte[] data, UUID targetPlayerId)`: Transmits raw audio data to a specific player identified by their UUID. The API handles the underlying network communication.
*   `onVoiceDataReceived(VoiceDataListener listener)`: Registers a `VoiceDataListener` to receive incoming voice data from other players. The listener will be invoked with the raw audio data and the UUID of the sender.
*   `playVoiceData(byte[] data, UUID sourcePlayerId)`: Plays raw audio data through the player's default audio output device.

**Example Usage:**

This example demonstrates how a mod could start/stop voice capture, send captured data, and listen for incoming voice data to play it back.

```java
import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModInitializer;
import com.example.modloader.api.VoiceAPI;
import com.example.modloader.api.VoiceDataListener;
import com.example.modloader.api.ModCommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MyVoiceChatMod implements ModInitializer {

    private VoiceAPI voiceAPI;
    private boolean isCapturing = false;

    @Override
    public void onLoad(ModAPI api) {
        this.voiceAPI = api.getVoiceAPI();

        // Register a command to toggle voice capture
        api.registerCommand("vc", new ModCommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, String commandLabel, String[] args) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this command.");
                    return true;
                }
                Player player = (Player) sender;

                if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
                    if (isCapturing) {
                        voiceAPI.stopVoiceCapture();
                        isCapturing = false;
                        player.sendMessage("§aVoice capture stopped.");
                    } else {
                        voiceAPI.startVoiceCapture();
                        isCapturing = true;
                        player.sendMessage("§aVoice capture started. Speak now!");
                    }
                    return true;
                }
                player.sendMessage("§cUsage: /vc toggle");
                return true;
            }

            @Override
            public List<String> onTabComplete(CommandSender sender, String alias, String[] args) {
                if (args.length == 1) {
                    return Collections.singletonList("toggle");
                }
                return Collections.emptyList();
            }
        });

        // Register a listener to play incoming voice data
        voiceAPI.onVoiceDataReceived(new VoiceDataListener() {
            @Override
            public void onVoiceData(byte[] data, UUID sourcePlayerId) {
                // In a real mod, you might add proximity checks here
                // For simplicity, we'll just play all incoming data
                voiceAPI.playVoiceData(data, sourcePlayerId);
            }
        });

        // Optional: If you want to send captured data to specific players,
        // you would typically do this in a separate thread or event handler
        // that gets the captured data from the VoiceAPI (e.g., via a custom event
        // or by extending VoiceAPIImpl to expose captured data).
        // For this example, the VoiceAPIImpl automatically captures and makes it available
        // to its internal network handler. Mod developers would call sendVoiceData explicitly.
    }

    @Override
    public void onDisable() {
        // Ensure voice capture is stopped when the mod is disabled
        if (isCapturing) {
            voiceAPI.stopVoiceCapture();
            isCapturing = false;
        }
    }
}
```

**Networking Details:**

The `VoiceAPI` utilizes the internal `Networking` API on the `modloader:voice` channel for transmitting and receiving audio data. This channel is configured for UDP communication to ensure low-latency real-time audio. Mod developers do not need to directly interact with this channel unless they wish to implement custom network handling for voice data.

**Mod Developer Responsibilities:**

While the `VoiceAPI` handles the core audio and network plumbing, mod developers are responsible for:

*   **Proximity Logic:** Determining which players should hear each other based on their in-game distance.
*   **Voice Channels:** Implementing logic for players to join different voice channels.
*   **User Interface:** Creating in-game indicators for who is speaking, volume controls, etc.
*   **Player Management:** Mapping UUIDs to in-game players and managing their voice states.
*   **Audio Codecs (Optional):** While the API transmits raw PCM data, mod developers could implement their own audio compression/decompression using external libraries if bandwidth is a concern, by processing the `byte[] data` before calling `sendVoiceData` and after receiving it.

---
### Mod Configuration

ModLoader999 provides an advanced, type-safe configuration system for your mods. This system abstracts away direct manipulation of `config.yml` files, allowing you to define configuration classes that implement the `ModConfig` interface. Fields within these classes, annotated with `@ConfigProperty`, are automatically bound to corresponding values in your mod's `config.yml`.

Your mod's `config.yml` file will be automatically extracted to `plugins/ModLoader999/configs/<your_mod_name>/config.yml` upon your mod's loading. Changes made to this file are live-reloaded, and your mod can be programmed to react dynamically to these configuration updates.

**1. Define Your Configuration Class:**

Create a class that implements `ModConfig` and define fields annotated with `@ConfigProperty`. Nested classes are fully supported for organizing complex configuration structures. The `@ConfigProperty` annotation offers powerful features like default values, validation (min/max, regex patterns, allowed values), and descriptions for documentation.

```java
package com.yourname.mod;

import com.example.modloader.api.config.ConfigProperty;
import com.example.modloader.api.config.ModConfig;

public class MyModConfig implements ModConfig {

    @ConfigProperty(path = "general.welcomeMessage", defaultValue = "Hello from MyMod!", description = "Message displayed to players on join.")
    public String welcomeMessage = "Hello from MyMod!";

    @ConfigProperty(path = "general.maxItems", defaultValue = "10", minValue = 1, maxValue = 100, description = "Maximum number of items allowed.")
    public int maxItems = 10;

    @ConfigProperty(path = "features.enableFeature", defaultValue = "true", description = "Enable or disable a specific feature.")
    public boolean enableFeature = true;

    @ConfigProperty(path = "difficulty", allowedValues = {"EASY", "NORMAL", "HARD"}, defaultValue = "NORMAL", description = "Mod difficulty setting.")
    public String difficulty = "NORMAL";

    @ConfigProperty(path = "contact.adminEmail", defaultValue = "admin@example.com", pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", description = "Admin email for notifications.")
    public String adminEmail = "admin@example.com";
}
```

**2. Provide Your Configuration Instance:**

Within your `ModInitializer` class, create a method annotated with `@ModConfigProvider`. This method should return an instance of your `ModConfig` class. ModLoader999 will invoke this method to obtain your mod's configuration instance. You can then retrieve the configured instance using `modAPI.getModConfig()`.

```java
package com.yourname.mod;

import com.example.modloader.api.ModAPI;
import com.example.modloader.api.ModInitializer;
import com.example.modloader.api.config.ModConfigProvider;
import com.example.modloader.api.dependencyinjection.Binder;

public class MyMod implements ModInitializer {

    private MyModConfig config;

    @ModConfigProvider
    public static MyModConfig provideConfig() {
        return new MyModConfig();
    }

    @Override
    public void configure(Binder binder) {
    }

    @Override
    public void onPreLoad(ModAPI api) {
        this.config = api.getModConfig(MyModConfig.class);
        System.out.println("Welcome message from config: " + config.welcomeMessage);
        System.out.println("Max items from config: " + config.maxItems);
    }

    // ... other lifecycle methods
}
```

**3. Accessing and Reacting to Changes:**

You can access your `ModConfig` instance directly (as demonstrated above) or retrieve it programmatically via `ModAPI.getModConfig()`. To enable your mod to react dynamically to live reloads of the `config.yml` file, you can register a `ConfigChangeListener`.

```java
import com.example.modloader.api.config.ConfigChangeListener;

// In your ModInitializer's onPreLoad(ModAPI api) method or similar
api.getModConfigManager().registerConfigChangeListener(api.getModId(), new ConfigChangeListener<MyModConfig>() {
    @Override
    public void onConfigChanged(MyModConfig newConfig) {
        MyMod.this.config = newConfig;
        System.out.println("Config reloaded! New welcome message: " + newConfig.welcomeMessage);
        // Apply new config settings to your mod's active components
    }
});

// To retrieve the current config instance at any point during your mod's operation:
// MyModConfig currentConfig = api.getModConfig(MyModConfig.class);
```

### Building Your Mod

1.  **Package:** Execute `mvn package` (for Maven projects) or `gradle build` (for Gradle projects) within your mod's project directory. This command will compile your source code and package it into a JAR file.
2.  **Rename:** Locate the generated `.jar` file, typically found in your `target/` (Maven) or `build/libs/` (Gradle) folder. It is crucial to rename its file extension from `.jar` to `.modloader999` (e.g., `MyAwesomeMod-1.0.0.jar` becomes `MyAwesomeMod-1.0.0.modloader999`).
3.  **Deploy:** Transfer the renamed `.modloader999` file into the `plugins/ModLoader999/Mods/` folder situated on your Minecraft server.
4.  **Reload:** Initiate a server restart or utilize the in-game command `/modloader reload` to load your newly deployed mod.

### Hot-Reloading for Faster Development

ModLoader999 provides robust support for a "soft" hot-reloading mechanism, specifically designed for individual mods. This feature significantly accelerates the development workflow by allowing for rapid iteration without the necessity of a full server restart. When you implement changes to your mod's codebase, you can leverage the `/modloader hotreload <modName>` command to swiftly update the mod within the live game environment.

**How it works:**

1.  **Temporary Disablement:** The specified mod is temporarily disabled, ensuring its current operations are gracefully halted.
2.  **Class Unloading:** Its existing classes are unloaded from the Java Virtual Machine (JVM) to the extent permitted by Java's class loading mechanisms.
3.  **Re-scanning and Reloading:** The mod's `.modloader999` JAR file is re-scanned, and its updated classes are loaded into memory.
4.  **Re-initialization and Re-enablement:** The mod is then re-initialized and re-enabled, reflecting your latest code changes almost instantaneously.

This streamlined process enables you to observe the effects of your code modifications with minimal delay, thereby substantially enhancing your development efficiency.

**Usage:**

After successfully compiling your mod and placing the updated `.modloader999` file into the `plugins/ModLoader999/Mods/` folder, simply execute the following command in-game:

```
/modloader hotreload MyAwesomeMod
```

Ensure you replace `MyAwesomeMod` with the actual, unique name of your mod.



---