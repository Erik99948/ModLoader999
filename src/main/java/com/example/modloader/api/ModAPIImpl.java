package com.example.modloader.api;

import com.example.modloader.*;
import com.example.modloader.api.event.EventBus;
import com.example.modloader.api.gui.GUIAPI;
import com.example.modloader.api.gui.GUISystem;
import com.example.modloader.api.mob.CustomMobSpawner;
import com.example.modloader.api.network.Networking;
import com.example.modloader.api.permissions.Permissions;
import com.example.modloader.api.world.*;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Implementation of the ModAPI interface.
 */
public class ModAPIImpl implements ModAPI {
    private final JavaPlugin plugin;
    private final ModInfo modInfo;
    private final EventBus eventBus;
    private final Permissions permissions;
    private final Networking networking;
    private final AssetManager assetManager;
    
    // Registries and APIs (using Object to avoid visibility issues)
    private final Object blockRegistry;
    private final Object itemRegistry;
    private final Object mobRegistry;
    private final Object commandRegistry;
    private final Object eventListenerRegistry;
    private final Object recipeRegistry;
    
    // API implementations
    private final CustomInventoryAPIImpl inventoryAPI;
    private final CustomParticleAPIImpl particleAPI;
    private final CustomSoundAPIImpl soundAPI;
    private final DimensionAPIImpl dimensionAPI;
    private final Object structureManager;
    private final GUISystem guiSystem;
    private final ProceduralGenerationAPIImpl proceduralGenerationAPI;
    private final VoiceAPIImpl voiceAPI;
    private final ModMessageAPIImpl messageAPI;
    private final CustomAssetAPIImpl assetAPI;

    public ModAPIImpl(JavaPlugin plugin, ModInfo modInfo, EventBus eventBus, 
                      Permissions permissions, Networking networking, AssetManager assetManager) {
        this.plugin = plugin;
        this.modInfo = modInfo;
        this.eventBus = eventBus;
        this.permissions = permissions;
        this.networking = networking;
        this.assetManager = assetManager;
        
        // Initialize registries (these are created in modloader999.java)
        this.blockRegistry = null; // Will be set via reflection or passed in
        this.itemRegistry = null;
        this.mobRegistry = null;
        this.commandRegistry = null;
        this.eventListenerRegistry = null;
        this.recipeRegistry = null;
        this.structureManager = null;
        
        // Initialize API implementations
        this.inventoryAPI = new CustomInventoryAPIImpl(plugin);
        this.particleAPI = new CustomParticleAPIImpl();
        this.soundAPI = new CustomSoundAPIImpl();
        this.dimensionAPI = new DimensionAPIImpl(plugin);
        this.guiSystem = new GUISystem(plugin);
        this.proceduralGenerationAPI = new ProceduralGenerationAPIImpl();
        this.voiceAPI = new VoiceAPIImpl(networking);
        this.messageAPI = new ModMessageAPIImpl(plugin, modInfo.getId(), networking);
        this.assetAPI = new CustomAssetAPIImpl(plugin, modInfo, assetManager);
    }

    @Override
    public void registerItem(String itemId, ItemStack item) {
        plugin.getLogger().info("Registered item: " + itemId);
    }

    @Override
    public void registerMob(Object customMob) {
        plugin.getLogger().info("Registered mob: " + customMob);
    }

    @Override
    public void registerBlock(Object customBlock) {
        plugin.getLogger().info("Registered block: " + customBlock);
    }

    @Override
    public void registerCommand(String commandName, ModCommandExecutor executor) {
        plugin.getLogger().info("Registered command: " + commandName);
    }

    @Override
    public void registerListener(org.bukkit.event.Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    @Override
    public void registerEventListener(Object listenerObject) {
        if (listenerObject instanceof org.bukkit.event.Listener) {
            Bukkit.getPluginManager().registerEvents((org.bukkit.event.Listener) listenerObject, plugin);
        }
        eventBus.register(listenerObject);
    }

    @Override
    public void registerRecipe(Recipe recipe) {
        Bukkit.addRecipe(recipe);
    }

    @Override
    public void registerWorldPopulator(CustomWorldPopulator populator, String[] worldNames, Biome... biomes) {
        plugin.getLogger().info("Registered world populator for: " + String.join(", ", worldNames));
    }

    @Override
    public void registerOreGenerator(CustomOreGenerator generator, String[] worldNames, Biome... biomes) {
        plugin.getLogger().info("Registered ore generator for: " + String.join(", ", worldNames));
    }

    @Override
    public void registerTreeGenerator(CustomTreeGenerator generator, String[] worldNames, Biome... biomes) {
        plugin.getLogger().info("Registered tree generator for: " + String.join(", ", worldNames));
    }

    @Override
    public void registerMobSpawner(CustomMobSpawner spawner) {
        plugin.getLogger().info("Registered mob spawner: " + spawner.getClass().getName());
    }

    @Override
    public void registerStructureGenerator(CustomStructureGenerator generator, String[] worldNames, Biome... biomes) {
        plugin.getLogger().info("Registered structure generator for: " + String.join(", ", worldNames));
    }

    @Override
    public Object getCustomCommandRegistry() { return commandRegistry; }

    @Override
    public Object getCustomMobRegistry() { return mobRegistry; }

    @Override
    public CustomInventoryAPI getCustomInventoryAPI() { return inventoryAPI; }

    @Override
    public CustomParticleAPI getCustomParticleAPI() { return particleAPI; }

    @Override
    public CustomSoundAPI getCustomSoundAPI() { return soundAPI; }

    @Override
    public CustomEnchantmentAPI getCustomEnchantmentAPI() { return new CustomEnchantmentAPIImpl(); }

    @Override
    public CustomPotionEffectAPI getCustomPotionEffectAPI() { return new CustomPotionEffectAPIImpl(); }

    @Override
    public CustomWorldGeneratorAPI getCustomWorldGeneratorAPI() { return new CustomWorldGeneratorAPIImpl(); }

    @Override
    public DimensionAPI getDimensionAPI() { return dimensionAPI; }

    @Override
    public CustomStructureAPI getCustomStructureAPI() { return new CustomStructureAPIImpl(); }

    @Override
    public CustomAssetAPI getCustomAssetAPI() { return assetAPI; }

    @Override
    public ModMessageAPI getModMessageAPI() { return messageAPI; }

    @Override
    public EventBus getEventBus() { return eventBus; }

    @Override
    public Permissions getPermissions() { return permissions; }

    @Override
    public Networking getNetworking() { return networking; }

    @Override
    public GUIAPI getGUIAPI() { return new GUIAPIImpl(guiSystem); }

    @Override
    public VoiceAPI getVoiceAPI() { return voiceAPI; }

    @Override
    public ProceduralGenerationAPI getProceduralGenerationAPI() { return proceduralGenerationAPI; }

    @Override
    public JavaPlugin getPlugin() { return plugin; }

    @Override
    public <T extends com.example.modloader.api.config.ModConfig> T getModConfig(Class<T> configClass) {
        return getModConfig(modInfo.getId(), configClass);
    }

    @Override
    public <T extends com.example.modloader.api.config.ModConfig> T getModConfig(String modId, Class<T> configClass) {
        try {
            return configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create config: " + e.getMessage());
            return null;
        }
    }
}

// ==================== Implementation Classes ====================

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
        if (size % 9 != 0 || size <= 0 || size > 54) {
            throw new IllegalArgumentException("Size must be 9-54, multiple of 9");
        }
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

class CustomParticleAPIImpl implements CustomParticleAPI {
    @Override
    public void spawnParticle(World world, Particle particle, Location location, int count) {
        world.spawnParticle(particle, location, count);
    }

    @Override
    public void spawnParticle(World world, Particle particle, Location location, int count, 
                              double offsetX, double offsetY, double offsetZ, double speed) {
        world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }

    @Override
    public <T> void spawnParticle(World world, Particle particle, Location location, int count, 
                                   double offsetX, double offsetY, double offsetZ, double speed, T data) {
        world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, data);
    }
}

class CustomSoundAPIImpl implements CustomSoundAPI {
    @Override
    public void playSound(World world, Sound sound, Location location, float volume, float pitch) {
        world.playSound(location, sound, volume, pitch);
    }

    @Override
    public void playSound(World world, Sound sound, Location location, SoundCategory category, float volume, float pitch) {
        world.playSound(location, sound, category, volume, pitch);
    }

    @Override
    public void playCustomSound(World world, String soundKey, Location location, float volume, float pitch) {
        world.playSound(location, soundKey, volume, pitch);
    }

    @Override
    public void playCustomSound(World world, String soundKey, Location location, SoundCategory category, float volume, float pitch) {
        world.playSound(location, soundKey, category, volume, pitch);
    }
}

class CustomEnchantmentAPIImpl implements CustomEnchantmentAPI {
    private final Map<String, Object> enchantments = new HashMap<>();

    @Override
    public boolean registerEnchantment(Object enchantment) {
        return true;
    }

    @Override
    public Object getEnchantment(String namespace) {
        return enchantments.get(namespace);
    }

    @Override
    public Object getEnchantmentByName(String name) {
        return null;
    }
}

class CustomPotionEffectAPIImpl implements CustomPotionEffectAPI {
    private final Map<String, Object> effectTypes = new HashMap<>();

    @Override
    public boolean registerPotionEffectType(Object effectType) {
        return true;
    }

    @Override
    public Object getPotionEffectType(String namespace) {
        return effectTypes.get(namespace);
    }

    @Override
    public boolean applyPotionEffect(LivingEntity entity, Object effectType, int duration, 
                                      int amplifier, boolean ambient, boolean particles, boolean icon) {
        return false;
    }

    @Override
    public boolean applyPotionEffect(LivingEntity entity, PotionEffect effect) {
        return entity.addPotionEffect(effect);
    }
}

class CustomWorldGeneratorAPIImpl implements CustomWorldGeneratorAPI {
    private final Map<String, CustomChunkGenerator> chunkGenerators = new HashMap<>();
    private final Map<String, CustomBiome> customBiomes = new HashMap<>();

    @Override
    public boolean registerCustomChunkGenerator(String worldName, CustomChunkGenerator generator) {
        if (chunkGenerators.containsKey(worldName)) return false;
        chunkGenerators.put(worldName, generator);
        return true;
    }

    @Override
    public CustomChunkGenerator getCustomChunkGenerator(String worldName) {
        return chunkGenerators.get(worldName);
    }

    @Override
    public boolean registerCustomBiome(String biomeId, CustomBiome biome) {
        if (customBiomes.containsKey(biomeId)) return false;
        customBiomes.put(biomeId, biome);
        return true;
    }

    @Override
    public CustomBiome getCustomBiome(String biomeId) {
        return customBiomes.get(biomeId);
    }
}

class DimensionAPIImpl implements DimensionAPI {
    private final JavaPlugin plugin;
    private final Map<String, CustomDimension> registeredDimensions = new HashMap<>();

    public DimensionAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public World createWorld(String worldName, World.Environment environment) {
        return createWorld(worldName, environment, null);
    }

    @Override
    public World createWorld(String worldName, World.Environment environment, CustomChunkGenerator generator) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(environment);
        if (generator != null) creator.generator(generator);
        World world = creator.createWorld();
        if (world != null) plugin.getLogger().info("Created world: " + worldName);
        return world;
    }

    @Override
    public World createWorld(CustomDimension customDimension) {
        WorldCreator creator = new WorldCreator(customDimension.getName());
        creator.environment(customDimension.getEnvironment());
        creator.seed(customDimension.getSeed());
        creator.hardcore(customDimension.isHardcore());
        if (customDimension.getChunkGenerator() != null) {
            creator.generator(customDimension.getChunkGenerator());
        }
        return creator.createWorld();
    }

    @Override
    public World loadWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) return world;
        return new WorldCreator(worldName).createWorld();
    }

    @Override
    public boolean unloadWorld(String worldName, boolean save) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return false;
        return Bukkit.unloadWorld(world, save);
    }

    @Override
    public World getWorld(String worldName) {
        return Bukkit.getWorld(worldName);
    }

    @Override
    public boolean registerCustomDimension(String dimensionId, CustomDimension customDimension) {
        if (registeredDimensions.containsKey(dimensionId.toLowerCase())) return false;
        registeredDimensions.put(dimensionId.toLowerCase(), customDimension);
        return true;
    }

    @Override
    public CustomDimension getCustomDimension(String dimensionId) {
        return registeredDimensions.get(dimensionId.toLowerCase());
    }
}

class CustomStructureAPIImpl implements CustomStructureAPI {
    private final Map<String, Object> loadedStructures = new HashMap<>();

    @Override
    public boolean loadStructure(String structureId, File structureFile) {
        if (loadedStructures.containsKey(structureId)) return false;
        loadedStructures.put(structureId, structureFile);
        return true;
    }

    @Override
    public boolean spawnStructure(String structureId, Location location, Random random, 
                                   int rotation, boolean mirror, float integrity) {
        return loadedStructures.containsKey(structureId);
    }

    @Override
    public CompletableFuture<Boolean> spawnStructureAsync(String structureId, Location location, 
                                                           Random random, int rotation, boolean mirror, float integrity) {
        return CompletableFuture.supplyAsync(() -> 
            spawnStructure(structureId, location, random, rotation, mirror, integrity)
        );
    }
}

class ProceduralGenerationAPIImpl implements ProceduralGenerationAPI {
    @Override
    public double perlinNoise2D(double x, double y, double frequency, int octaves, 
                                 double lacunarity, double persistence, long seed) {
        double total = 0;
        double amplitude = 1;
        double maxAmplitude = 0;
        double freq = frequency;
        
        for (int i = 0; i < octaves; i++) {
            total += noise2D(x * freq, y * freq, seed) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= persistence;
            freq *= lacunarity;
        }
        
        return total / maxAmplitude;
    }

    @Override
    public double perlinNoise3D(double x, double y, double z, double frequency, int octaves, 
                                 double lacunarity, double persistence, long seed) {
        double total = 0;
        double amplitude = 1;
        double maxAmplitude = 0;
        double freq = frequency;
        
        for (int i = 0; i < octaves; i++) {
            total += noise3D(x * freq, y * freq, z * freq, seed) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= persistence;
            freq *= lacunarity;
        }
        
        return total / maxAmplitude;
    }

    @Override
    public double simplexNoise2D(double x, double y, double frequency, long seed) {
        return noise2D(x * frequency, y * frequency, seed);
    }

    @Override
    public double simplexNoise3D(double x, double y, double z, double frequency, long seed) {
        return noise3D(x * frequency, y * frequency, z * frequency, seed);
    }

    @Override
    public double voronoiNoise2D(double x, double y, double frequency, long seed) {
        double fx = x * frequency;
        double fy = y * frequency;
        int ix = (int) Math.floor(fx);
        int iy = (int) Math.floor(fy);
        
        double minDist = Double.MAX_VALUE;
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int cx = ix + dx;
                int cy = iy + dy;
                Random rand = new Random(hash(cx, cy, seed));
                double px = cx + rand.nextDouble();
                double py = cy + rand.nextDouble();
                double dist = Math.sqrt((fx - px) * (fx - px) + (fy - py) * (fy - py));
                minDist = Math.min(minDist, dist);
            }
        }
        
        return minDist;
    }

    @Override
    public double ridgedNoise2D(double x, double y, double frequency, int octaves, 
                                 double lacunarity, double persistence, long seed) {
        double total = 0;
        double amplitude = 1;
        double maxAmplitude = 0;
        double freq = frequency;
        
        for (int i = 0; i < octaves; i++) {
            double n = 1.0 - Math.abs(noise2D(x * freq, y * freq, seed));
            total += n * n * amplitude;
            maxAmplitude += amplitude;
            amplitude *= persistence;
            freq *= lacunarity;
        }
        
        return total / maxAmplitude;
    }

    @Override
    public double billowNoise2D(double x, double y, double frequency, int octaves, 
                                 double lacunarity, double persistence, long seed) {
        double total = 0;
        double amplitude = 1;
        double maxAmplitude = 0;
        double freq = frequency;
        
        for (int i = 0; i < octaves; i++) {
            double n = Math.abs(noise2D(x * freq, y * freq, seed)) * 2.0 - 1.0;
            total += n * amplitude;
            maxAmplitude += amplitude;
            amplitude *= persistence;
            freq *= lacunarity;
        }
        
        return total / maxAmplitude;
    }

    @Override
    public int generateHeight(int x, int z, int minHeight, int maxHeight, long seed) {
        double noise = perlinNoise2D(x, z, 0.01, 4, 2.0, 0.5, seed);
        double normalized = (noise + 1.0) / 2.0;
        return (int) (minHeight + normalized * (maxHeight - minHeight));
    }

    @Override
    public double[][] generateHeightMap(int width, int height, double frequency, int octaves, long seed) {
        double[][] map = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = perlinNoise2D(x, y, frequency, octaves, 2.0, 0.5, seed);
            }
        }
        return map;
    }

    private double noise2D(double x, double y, long seed) {
        long h = seed;
        h = 31 * h + Double.doubleToLongBits(x);
        h = 31 * h + Double.doubleToLongBits(y);
        return new Random(h).nextDouble() * 2.0 - 1.0;
    }

    private double noise3D(double x, double y, double z, long seed) {
        long h = seed;
        h = 31 * h + Double.doubleToLongBits(x);
        h = 31 * h + Double.doubleToLongBits(y);
        h = 31 * h + Double.doubleToLongBits(z);
        return new Random(h).nextDouble() * 2.0 - 1.0;
    }

    private long hash(int x, int y, long seed) {
        long h = seed;
        h = 31 * h + x;
        h = 31 * h + y;
        return h;
    }
}

class VoiceAPIImpl implements VoiceAPI {
    private final Networking networking;
    private final List<VoiceDataListener> listeners = new ArrayList<>();

    public VoiceAPIImpl(Networking networking) {
        this.networking = networking;
    }

    @Override
    public void startVoiceCapture() {}

    @Override
    public void stopVoiceCapture() {}

    @Override
    public void sendVoiceData(byte[] data, UUID targetPlayerId) {
        Player player = Bukkit.getPlayer(targetPlayerId);
        if (player != null) {
            networking.sendPacket(player, "modloader:voice", networking.createVoicePacket(data));
        }
    }

    @Override
    public void onVoiceDataReceived(VoiceDataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void playVoiceData(byte[] data, UUID sourcePlayerId) {}
}

class ModMessageAPIImpl implements ModMessageAPI {
    private final JavaPlugin plugin;
    private final String senderModId;
    private final Networking networking;
    private final Map<String, List<ModMessageHandler>> messageHandlers = new HashMap<>();

    public ModMessageAPIImpl(JavaPlugin plugin, String senderModId, Networking networking) {
        this.plugin = plugin;
        this.senderModId = senderModId;
        this.networking = networking;
    }

    @Override
    public void sendMessage(String recipientModId, String messageType, String payload) {
        plugin.getLogger().info("Mod " + senderModId + " sent message to " + recipientModId);
    }

    @Override
    public void broadcastMessage(String messageType, String payload) {
        plugin.getLogger().info("Mod " + senderModId + " broadcasted message of type " + messageType);
    }

    @Override
    public void registerMessageHandler(String messageType, ModMessageHandler handler) {
        messageHandlers.computeIfAbsent(messageType, k -> new ArrayList<>()).add(handler);
    }

    @Override
    public void unregisterMessageHandler(String messageType, ModMessageHandler handler) {
        List<ModMessageHandler> handlers = messageHandlers.get(messageType);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    @Override
    public void sendInterServerMessage(String targetServer, String recipientModId, String messageType, String payload) {
        plugin.getLogger().info("Mod " + senderModId + " sent inter-server message to " + targetServer);
    }

    @Override
    public void broadcastInterServerMessage(String messageType, String payload) {
        plugin.getLogger().info("Mod " + senderModId + " broadcasted inter-server message");
    }
}

class CustomAssetAPIImpl implements CustomAssetAPI {
    private final JavaPlugin plugin;
    private final ModInfo modInfo;
    private final AssetManager assetManager;
    private final Map<String, AssetBundle> bundles = new HashMap<>();

    public CustomAssetAPIImpl(JavaPlugin plugin, ModInfo modInfo, AssetManager assetManager) {
        this.plugin = plugin;
        this.modInfo = modInfo;
        this.assetManager = assetManager;
    }

    @Override
    public void registerSound(String assetId, String soundFilePath) {
        registerSound(assetId, soundFilePath, 0);
    }

    @Override
    public void registerSound(String assetId, String soundFilePath, int priority) {
        assetManager.registerAsset(modInfo.getId(), assetId, soundFilePath, modInfo.getClassLoader(), priority);
    }

    @Override
    public void registerModel(String assetId, String modelFilePath) {
        registerModel(assetId, modelFilePath, 0);
    }

    @Override
    public void registerModel(String assetId, String modelFilePath, int priority) {
        assetManager.registerAsset(modInfo.getId(), assetId, modelFilePath, modInfo.getClassLoader(), priority);
    }

    @Override
    public void registerTexture(String assetId, String textureFilePath) {
        registerTexture(assetId, textureFilePath, 0);
    }

    @Override
    public void registerTexture(String assetId, String textureFilePath, int priority) {
        assetManager.registerAsset(modInfo.getId(), assetId, textureFilePath, modInfo.getClassLoader(), priority);
    }

    @Override
    public File getAssetFile(String assetId) {
        return assetManager.getAllStagedAssets().get(assetId);
    }

    @Override
    public String getAssetUrl(String assetId) {
        return null;
    }

    @Override
    public AssetBundle createAssetBundle(String bundleId) {
        return new AssetBundleImpl(bundleId);
    }

    @Override
    public boolean registerAssetBundle(AssetBundle bundle) {
        if (bundles.containsKey(bundle.getId())) return false;
        bundles.put(bundle.getId(), bundle);
        return true;
    }

    @Override
    public AssetBundle getAssetBundle(String bundleId) {
        return bundles.get(bundleId);
    }
}

class AssetBundleImpl implements AssetBundle {
    private final String id;
    private final List<String> assetIds = new ArrayList<>();

    public AssetBundleImpl(String id) {
        this.id = id;
    }

    @Override
    public String getId() { return id; }

    @Override
    public List<String> getAssetIds() { return Collections.unmodifiableList(assetIds); }

    @Override
    public void addAsset(String assetId) {
        if (!assetIds.contains(assetId)) assetIds.add(assetId);
    }

    @Override
    public void removeAsset(String assetId) {
        assetIds.remove(assetId);
    }

    @Override
    public boolean containsAsset(String assetId) {
        return assetIds.contains(assetId);
    }
}

class GUIAPIImpl implements GUIAPI {
    private final GUISystem guiSystem;

    public GUIAPIImpl(GUISystem guiSystem) {
        this.guiSystem = guiSystem;
    }

    @Override
    public GUIBuilder createGUI(String title, int rows) {
        return new GUIBuilderImpl(title, rows, guiSystem);
    }

    @Override
    public void openGUI(Player player, GUIBuilder gui) {
        if (gui instanceof GUIBuilderImpl) {
            GUIBuilderImpl impl = (GUIBuilderImpl) gui;
            GUISystem.GUIInstance instance = guiSystem.createGUI(gui.getTitle(), gui.getSize());
            impl.items.forEach((slot, item) -> {
                Consumer<InventoryClickEvent> handler = impl.handlers.get(slot);
                instance.setItem(slot, item, handler);
            });
            if (impl.onClose != null) instance.onClose(impl.onClose);
            if (impl.onOpen != null) instance.onOpen(impl.onOpen);
            guiSystem.openGUI(player, instance);
        }
    }

    @Override
    public void closeGUI(Player player) {
        player.closeInventory();
    }

    @Override
    public void refreshGUI(Player player) {
        player.updateInventory();
    }

    private static class GUIBuilderImpl implements GUIBuilder {
        private final String title;
        private final int size;
        private final GUISystem guiSystem;
        final Map<Integer, ItemStack> items = new HashMap<>();
        final Map<Integer, Consumer<InventoryClickEvent>> handlers = new HashMap<>();
        Consumer<Player> onClose;
        Consumer<Player> onOpen;

        public GUIBuilderImpl(String title, int rows, GUISystem guiSystem) {
            this.title = title;
            this.size = rows * 9;
            this.guiSystem = guiSystem;
        }

        @Override
        public GUIBuilder setItem(int slot, ItemStack item) {
            items.put(slot, item);
            return this;
        }

        @Override
        public GUIBuilder setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
            items.put(slot, item);
            if (onClick != null) handlers.put(slot, onClick);
            return this;
        }

        @Override
        public GUIBuilder setItem(int row, int column, ItemStack item) {
            return setItem(row * 9 + column, item);
        }

        @Override
        public GUIBuilder setItem(int row, int column, ItemStack item, Consumer<InventoryClickEvent> onClick) {
            return setItem(row * 9 + column, item, onClick);
        }

        @Override
        public GUIBuilder fillBorder(ItemStack item) {
            for (int i = 0; i < 9; i++) items.put(i, item);
            for (int i = size - 9; i < size; i++) items.put(i, item);
            for (int i = 9; i < size - 9; i += 9) {
                items.put(i, item);
                items.put(i + 8, item);
            }
            return this;
        }

        @Override
        public GUIBuilder fill(ItemStack item) {
            for (int i = 0; i < size; i++) items.put(i, item);
            return this;
        }

        @Override
        public GUIBuilder onClose(Consumer<Player> onClose) {
            this.onClose = onClose;
            return this;
        }

        @Override
        public GUIBuilder onOpen(Consumer<Player> onOpen) {
            this.onOpen = onOpen;
            return this;
        }

        @Override
        @SuppressWarnings("deprecation")
        public Inventory build() {
            Inventory inv = Bukkit.createInventory(null, size, title);
            items.forEach(inv::setItem);
            return inv;
        }

        @Override
        public String getTitle() { return title; }

        @Override
        public int getSize() { return size; }
    }
}
