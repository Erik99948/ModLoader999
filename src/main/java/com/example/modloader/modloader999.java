package com.example.modloader;

import com.example.modloader.api.CustomInventoryAPI;
import com.example.modloader.api.block.*;
import com.example.modloader.api.event.EventBus;
import com.example.modloader.api.mob.CustomMobGoal;
import com.example.modloader.api.mob.CustomMobSpawner;
import com.example.modloader.api.world.CustomWorldPopulator;
import com.example.modloader.mob.CustomMobGoalExecutor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Main plugin class for ModLoader999
 */
public final class modloader999 extends JavaPlugin {
    private ModLoaderService modLoaderService;
    private WebServer webServer;

    @Override
    public void onEnable() {
        getLogger().info("ModLoader999 plugin is enabling!");
        
        File modsFolder = new File(getDataFolder(), "Mods");
        if (!modsFolder.exists()) {
            if (modsFolder.mkdirs()) {
                getLogger().info("Created Mods folder at: " + modsFolder.getPath());
            } else {
                getLogger().severe("Failed to create Mods folder!");
                return;
            }
        }
        
        File publishedModsFolder = new File(getDataFolder(), "published_mods");
        if (!publishedModsFolder.exists()) {
            if (publishedModsFolder.mkdirs()) {
                getLogger().info("Created published_mods folder at: " + publishedModsFolder.getPath());
            } else {
                getLogger().severe("Failed to create published_mods folder!");
                return;
            }
        }
        
        saveResource("mod.policy", false);
        saveDefaultConfig();
        
        int webServerPort = getConfig().getInt("web-server-port", 25566);
        
        this.modLoaderService = new ModLoaderService(this);
        this.modLoaderService.loadModsAndGeneratePack();
        
        if (!this.modLoaderService.getResourcePackGenerator().generate()) {
            getLogger().severe("Could not generate resource pack. Aborting resource pack server startup.");
            return;
        }
        
        this.webServer = new WebServer(this, this.modLoaderService, 
            this.modLoaderService.getResourcePackGenerator().getZipFile(), webServerPort, modsFolder);
        this.webServer.start();
        
        getCommand("modloader").setExecutor(new ModLoaderCommandExecutor(this, this.modLoaderService, this.webServer));
    }

    @Override
    public void onDisable() {
        getLogger().info("ModLoader999 plugin is disabling!");
        if (this.webServer != null) {
            this.webServer.stop();
        }
        if (this.modLoaderService != null) {
            this.modLoaderService.disableMods();
        }
    }
}

// ==================== Custom Block Registry ====================

class CustomBlockRegistry implements Listener {
    private final Plugin plugin;
    private final Logger logger;
    private final Map<String, CustomBlock> registeredCustomBlocks = new HashMap<>();
    private final NamespacedKey customBlockIdKey;
    private final EventBus eventBus;

    public CustomBlockRegistry(Plugin plugin, EventBus eventBus) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.customBlockIdKey = new NamespacedKey(plugin, "custom_block_id");
        this.eventBus = eventBus;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void register(CustomBlock customBlock) {
        if (registeredCustomBlocks.containsKey(customBlock.getId())) {
            logger.warning("Custom block with ID '" + customBlock.getId() + "' already registered. Skipping.");
            return;
        }
        registeredCustomBlocks.put(customBlock.getId(), customBlock);
        logger.info("Registered custom block: " + customBlock.getId() + " (based on " + customBlock.getBaseMaterial().name() + ")");
    }

    public CustomBlock getCustomBlock(String id) {
        return registeredCustomBlocks.get(id);
    }

    public CustomBlock getCustomBlockFromWorldBlock(Block block) {
        BlockState state = block.getState();
        if (state instanceof TileState) {
            PersistentDataContainer container = ((TileState) state).getPersistentDataContainer();
            if (container.has(customBlockIdKey, PersistentDataType.STRING)) {
                String customBlockId = container.get(customBlockIdKey, PersistentDataType.STRING);
                return registeredCustomBlocks.get(customBlockId);
            }
        }
        return null;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemInHand = event.getItemInHand();
        CustomBlock customBlock = getCustomBlockFromItemStack(itemInHand);
        if (customBlock != null) {
            Block placedBlock = event.getBlockPlaced();
            BlockState state = placedBlock.getState();
            if (state instanceof TileState) {
                ((TileState) state).getPersistentDataContainer().set(customBlockIdKey, PersistentDataType.STRING, customBlock.getId());
                state.update(true);
            }
            BlockPlaceBehavior behavior = customBlock.getPlaceBehavior();
            if (behavior != null) {
                behavior.onPlace(event, placedBlock, event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        CustomBlock customBlock = getCustomBlockFromWorldBlock(brokenBlock);
        if (customBlock != null) {
            BlockBreakBehavior behavior = customBlock.getBreakBehavior();
            if (behavior != null) {
                behavior.onBreak(event, brokenBlock, event.getPlayer());
            }
            if (!event.isCancelled()) {
                event.setDropItems(false);
                List<ItemStack> drops = customBlock.getCustomDrops();
                if (drops.isEmpty()) {
                    brokenBlock.getWorld().dropItemNaturally(brokenBlock.getLocation(), customBlock.getItemStack());
                } else {
                    for (ItemStack drop : drops) {
                        brokenBlock.getWorld().dropItemNaturally(brokenBlock.getLocation(), drop);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Block clickedBlock = event.getClickedBlock();
        CustomBlock customBlock = getCustomBlockFromWorldBlock(clickedBlock);
        if (customBlock != null) {
            BlockInteractBehavior behavior = customBlock.getInteractBehavior();
            if (behavior != null) {
                behavior.onInteract(event, clickedBlock, event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        CustomBlock customBlock = getCustomBlockFromWorldBlock(block);
        if (customBlock != null) {
            BlockRedstoneBehavior behavior = customBlock.getRedstoneBehavior();
            if (behavior != null) {
                behavior.onRedstone(event, block, event.getOldCurrent(), event.getNewCurrent());
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> blocks = new ArrayList<>(event.blockList());
        for (Block block : blocks) {
            CustomBlock customBlock = getCustomBlockFromWorldBlock(block);
            if (customBlock != null) {
                BlockExplodeBehavior behavior = customBlock.getExplodeBehavior();
                if (behavior != null) {
                    behavior.onExplode(event, block, event.blockList(), event.getYield());
                }
            }
        }
    }

    private CustomBlock getCustomBlockFromItemStack(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) return null;
        for (CustomBlock customBlock : registeredCustomBlocks.values()) {
            if (customBlock.getCustomModelData() == meta.getCustomModelData() &&
                customBlock.getBaseMaterial() == itemStack.getType()) {
                return customBlock;
            }
        }
        return null;
    }

    public void unregisterAll() {
        registeredCustomBlocks.clear();
        org.bukkit.event.HandlerList.unregisterAll(this);
    }
}

// ==================== Custom Command Registry ====================

class CustomCommandRegistry {
    private final Plugin plugin;
    private final Logger logger;
    private CommandMap commandMap;
    private final Map<String, String> registeredCommandMods = new HashMap<>();
    private final Map<String, Command> registeredCommands = new HashMap<>();

    @SuppressWarnings("deprecation")
    public CustomCommandRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        try {
            if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);
                this.commandMap = (CommandMap) f.get(plugin.getServer().getPluginManager());
            }
        } catch (Exception e) {
            logger.severe("Failed to get Bukkit CommandMap: " + e.getMessage());
        }
    }

    public void register(String commandName, com.example.modloader.api.ModCommandExecutor modExecutor, String modId) {
        if (commandMap == null) {
            logger.warning("CommandMap not available, cannot register command: " + commandName);
            return;
        }
        if (registeredCommandMods.containsKey(commandName)) {
            logger.warning("Command '" + commandName + "' already registered. Skipping.");
            return;
        }
        Command bukkitCommand = new Command(commandName) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                return modExecutor.onCommand(sender, commandLabel, args);
            }
            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                List<String> completions = modExecutor.onTabComplete(sender, alias, args);
                return completions != null ? completions : super.tabComplete(sender, alias, args);
            }
        };
        commandMap.register(plugin.getName(), bukkitCommand);
        registeredCommandMods.put(commandName, modId);
        registeredCommands.put(commandName, bukkitCommand);
        logger.info("Registered custom command: /" + commandName + " by mod " + modId);
    }

    public void unregisterAll(String modId) {
        if (commandMap == null) return;
        List<String> commandsToRemove = new ArrayList<>();
        for (Map.Entry<String, String> entry : registeredCommandMods.entrySet()) {
            if (entry.getValue().equals(modId)) {
                commandsToRemove.add(entry.getKey());
            }
        }
        for (String commandName : commandsToRemove) {
            Command command = registeredCommands.get(commandName);
            if (command != null) {
                command.unregister(commandMap);
                registeredCommands.remove(commandName);
                registeredCommandMods.remove(commandName);
            }
        }
    }

    public void unregisterAll() {
        if (commandMap == null) return;
        for (String commandName : new ArrayList<>(registeredCommands.keySet())) {
            Command command = registeredCommands.get(commandName);
            if (command != null) {
                command.unregister(commandMap);
            }
        }
        registeredCommands.clear();
        registeredCommandMods.clear();
    }
}

// ==================== Custom Event Listener Registry ====================

class CustomEventListenerRegistry {
    private final Plugin plugin;
    private final Logger logger;
    private final EventBus eventBus;
    private final Map<Object, String> registeredListenerMods = new HashMap<>();
    private final List<Object> registeredListeners = new ArrayList<>();

    public CustomEventListenerRegistry(Plugin plugin, EventBus eventBus) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.eventBus = eventBus;
    }

    public void register(Object listener, String modId) {
        if (listener instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) listener, plugin);
        }
        eventBus.register(listener);
        registeredListeners.add(listener);
        registeredListenerMods.put(listener, modId);
        logger.info("Registered event listener: " + listener.getClass().getName() + " by mod " + modId);
    }

    public void unregisterAll(String modId) {
        List<Object> listenersToRemove = new ArrayList<>();
        for (Map.Entry<Object, String> entry : registeredListenerMods.entrySet()) {
            if (entry.getValue().equals(modId)) {
                listenersToRemove.add(entry.getKey());
            }
        }
        for (Object listener : listenersToRemove) {
            if (listener instanceof Listener) {
                org.bukkit.event.HandlerList.unregisterAll((Listener) listener);
            }
            eventBus.unregister(listener);
            registeredListeners.remove(listener);
            registeredListenerMods.remove(listener);
        }
    }

    public void unregisterAll() {
        for (Object listener : new ArrayList<>(registeredListeners)) {
            if (listener instanceof Listener) {
                org.bukkit.event.HandlerList.unregisterAll((Listener) listener);
            }
            eventBus.unregister(listener);
        }
        registeredListeners.clear();
        registeredListenerMods.clear();
    }
}

// ==================== Custom Inventory Implementation ====================

class CustomInventoryAPIImpl implements CustomInventoryAPI {
    private final JavaPlugin plugin;
    private final Map<Inventory, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();
    private final Map<Inventory, Consumer<InventoryCloseEvent>> closeHandlers = new HashMap<>();

    public CustomInventoryAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Inventory createInventory(int size, String title) {
        if (size % 9 != 0 || size <= 0 || size > 54) throw new IllegalArgumentException("Size must be 9-54, multiple of 9");
        return Bukkit.createInventory(null, size, title);
    }

    @Override
    public void openInventory(Player player, Inventory inventory) {
        player.openInventory(inventory);
    }

    @Override
    public void registerClickHandler(Inventory inventory, Consumer<InventoryClickEvent> clickHandler) {
        clickHandlers.put(inventory, clickHandler);
    }

    @Override
    public void registerCloseHandler(Inventory inventory, Consumer<InventoryCloseEvent> closeHandler) {
        closeHandlers.put(inventory, closeHandler);
    }

    @Override
    public void setItem(Inventory inventory, int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    @Override
    public ItemStack getItem(Inventory inventory, int slot) {
        return inventory.getItem(slot);
    }

    public void handleClick(InventoryClickEvent event) {
        Consumer<InventoryClickEvent> handler = clickHandlers.get(event.getInventory());
        if (handler != null) handler.accept(event);
    }

    public void handleClose(InventoryCloseEvent event) {
        Consumer<InventoryCloseEvent> handler = closeHandlers.get(event.getInventory());
        if (handler != null) handler.accept(event);
    }

    public boolean isCustomInventory(Inventory inventory) {
        return clickHandlers.containsKey(inventory) || closeHandlers.containsKey(inventory);
    }
}

class CustomInventoryListener implements Listener {
    private final CustomInventoryAPIImpl customInventoryAPI;

    public CustomInventoryListener(CustomInventoryAPIImpl customInventoryAPI, JavaPlugin plugin) {
        this.customInventoryAPI = customInventoryAPI;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (customInventoryAPI.isCustomInventory(event.getInventory())) {
            customInventoryAPI.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (customInventoryAPI.isCustomInventory(event.getInventory())) {
            customInventoryAPI.handleClose(event);
        }
    }
}

// ==================== Custom Item Registry ====================

class CustomItemRegistry {
    private final Logger logger;
    private final Map<String, ItemStack> registeredItems = new HashMap<>();
    private final EventBus eventBus;

    public CustomItemRegistry(Plugin plugin, EventBus eventBus) {
        this.logger = plugin.getLogger();
        this.eventBus = eventBus;
    }

    public void register(String itemId, ItemStack item) {
        if (registeredItems.containsKey(itemId)) {
            logger.warning("Custom item with ID '" + itemId + "' already registered. Skipping.");
            return;
        }
        registeredItems.put(itemId, item);
        logger.info("Registered custom item: " + itemId);
    }

    public ItemStack getItemStack(String id) {
        return registeredItems.get(id);
    }
}

// ==================== Custom Mob Registry ====================

class CustomMobRegistry implements Listener {
    private final Plugin plugin;
    private final Logger logger;
    private final Map<String, CustomMob> registeredCustomMobs = new HashMap<>();
    private final Map<LivingEntity, CustomMobGoalExecutor> goalExecutors = new HashMap<>();
    private final List<CustomMobSpawner> registeredSpawners = new ArrayList<>();
    private final NamespacedKey customMobIdKey;
    private final EventBus eventBus;

    public CustomMobRegistry(Plugin plugin, EventBus eventBus) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.customMobIdKey = new NamespacedKey(plugin, "custom_mob_id");
        this.eventBus = eventBus;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (CustomMobGoalExecutor executor : goalExecutors.values()) {
                    executor.tick();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public void register(CustomMob customMob) {
        if (registeredCustomMobs.containsKey(customMob.getId())) {
            logger.warning("Custom mob with ID '" + customMob.getId() + "' already registered. Skipping.");
            return;
        }
        registeredCustomMobs.put(customMob.getId(), customMob);
        logger.info("Registered custom mob: " + customMob.getId() + " (based on " + customMob.getBaseType().name() + ")");
    }

    public void registerSpawner(CustomMobSpawner spawner) {
        registeredSpawners.add(spawner);
        logger.info("Registered custom mob spawner: " + spawner.getClass().getName());
    }

    public CustomMob getCustomMob(String id) {
        return registeredCustomMobs.get(id);
    }

    @SuppressWarnings("deprecation")
    public org.bukkit.entity.Entity spawn(String customMobId, Location location) {
        CustomMob customMob = getCustomMob(customMobId);
        if (customMob == null || location.getWorld() == null) return null;
        
        org.bukkit.entity.Entity entity = location.getWorld().spawnEntity(location, customMob.getBaseType());
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.getPersistentDataContainer().set(customMobIdKey, PersistentDataType.STRING, customMobId);
            livingEntity.setCustomName(customMob.getName());
            livingEntity.setCustomNameVisible(true);
            
            AttributeInstance healthAttr = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(customMob.getMaxHealth());
                livingEntity.setHealth(customMob.getMaxHealth());
            }
            
            goalExecutors.put(livingEntity, new CustomMobGoalExecutor(livingEntity, customMob.getGoals()));
            return livingEntity;
        }
        return entity;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        Random random = new Random();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int highestY = world.getHighestBlockYAt(chunk.getX() * 16 + x, chunk.getZ() * 16 + z);
                Block spawnBlock = world.getBlockAt(chunk.getX() * 16 + x, highestY + 1, chunk.getZ() * 16 + z);
                for (CustomMobSpawner spawner : registeredSpawners) {
                    spawner.spawn(world, random, spawnBlock);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        goalExecutors.remove(entity);
    }

    public void unregisterAll() {
        registeredCustomMobs.clear();
        goalExecutors.clear();
        registeredSpawners.clear();
        org.bukkit.event.HandlerList.unregisterAll(this);
    }
}

// ==================== Custom Recipe Registry ====================

class CustomRecipeRegistry {
    private final Plugin plugin;
    private final Logger logger;
    private final List<Recipe> registeredRecipes = new ArrayList<>();

    public CustomRecipeRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void register(Recipe recipe) {
        Bukkit.addRecipe(recipe);
        registeredRecipes.add(recipe);
        logger.info("Registered custom recipe: " + recipe.getResult().getType().name());
    }

    public void unregisterAll() {
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            if (registeredRecipes.contains(recipe)) {
                it.remove();
            }
        }
        registeredRecipes.clear();
    }
}

// ==================== World Generation Classes ====================

class CustomOreWorldPopulator implements CustomWorldPopulator {
    private final org.bukkit.block.data.BlockData oreBlockData;
    private final int minY;
    private final int maxY;
    private final double chancePerChunk;
    private final int minVeinSize;
    private final int maxVeinSize;

    public CustomOreWorldPopulator(org.bukkit.block.data.BlockData oreBlockData, int minY, int maxY, 
                                    double chancePerChunk, int minVeinSize, int maxVeinSize) {
        this.oreBlockData = oreBlockData;
        this.minY = minY;
        this.maxY = maxY;
        this.chancePerChunk = chancePerChunk;
        this.minVeinSize = minVeinSize;
        this.maxVeinSize = maxVeinSize;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (random.nextDouble() < chancePerChunk) {
            int veins = random.nextInt(maxVeinSize - minVeinSize + 1) + minVeinSize;
            for (int i = 0; i < veins; i++) {
                int x = random.nextInt(16);
                int z = random.nextInt(16);
                int y = random.nextInt(maxY - minY + 1) + minY;
                Block block = chunk.getBlock(x, y, z);
                if (block.getType() == Material.STONE || block.getType() == Material.DEEPSLATE) {
                    block.setBlockData(oreBlockData);
                }
            }
        }
    }
}

// ==================== Structure Classes ====================

class SimpleStructure {
    private final Map<org.bukkit.util.Vector, org.bukkit.block.data.BlockData> blocks = new HashMap<>();
    private final org.bukkit.util.Vector size;

    public SimpleStructure(org.bukkit.util.Vector size) {
        this.size = size;
    }

    public void setBlock(int x, int y, int z, Material material) {
        blocks.put(new org.bukkit.util.Vector(x, y, z), material.createBlockData());
    }

    public Map<org.bukkit.util.Vector, org.bukkit.block.data.BlockData> getBlocks() { return blocks; }
    public org.bukkit.util.Vector getSize() { return size; }
}

class CustomStructureManager {
    private final JavaPlugin plugin;
    private final Map<String, SimpleStructure> loadedStructures = new HashMap<>();

    public CustomStructureManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean loadStructure(String structureId, File structureFile) {
        if (loadedStructures.containsKey(structureId)) return false;
        SimpleStructure simpleStructure = new SimpleStructure(new org.bukkit.util.Vector(3, 3, 3));
        simpleStructure.setBlock(0, 0, 0, Material.STONE);
        loadedStructures.put(structureId, simpleStructure);
        return true;
    }

    public boolean spawnStructure(String structureId, Location location, Random random, int rotation, boolean mirror, float integrity) {
        SimpleStructure structure = loadedStructures.get(structureId);
        if (structure == null || location.getWorld() == null) return false;
        
        for (Map.Entry<org.bukkit.util.Vector, org.bukkit.block.data.BlockData> entry : structure.getBlocks().entrySet()) {
            Location targetLocation = location.clone().add(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ());
            targetLocation.getBlock().setBlockData(entry.getValue(), false);
        }
        return true;
    }
}
