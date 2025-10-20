package com.example.modloader;

import com.example.modloader.mob.CustomMobGoalExecutor;
import com.example.modloader.api.mob.CustomMobSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class CustomMobRegistry implements Listener {

    private final Plugin plugin;
    private final Logger logger;
    private final Map<String, CustomMob> registeredCustomMobs = new HashMap<>();
    private final Map<LivingEntity, CustomMobGoalExecutor> goalExecutors = new HashMap<>();
    private final List<CustomMobSpawner> registeredSpawners = new ArrayList<>();
    private final NamespacedKey customMobIdKey;

    public CustomMobRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.customMobIdKey = new NamespacedKey(plugin, "custom_mob_id");

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

    public Entity spawn(String customMobId, Location location) {
        CustomMob customMob = getCustomMob(customMobId);
        if (customMob == null) {
            logger.warning("Custom mob with ID '" + customMobId + "' not found. Cannot spawn.");
            return null;
        }

        if (location.getWorld() == null) {
            logger.warning("Cannot spawn custom mob in a null world.");
            return null;
        }

        Entity entity = location.getWorld().spawnEntity(location, customMob.getBaseType());
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.getPersistentDataContainer().set(customMobIdKey, PersistentDataType.STRING, customMobId);
            livingEntity.setCustomName(customMob.getName());
            livingEntity.setCustomNameVisible(true);
            AttributeInstance healthAttribute = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (healthAttribute != null) {
                healthAttribute.setBaseValue(customMob.getMaxHealth());
                livingEntity.setHealth(customMob.getMaxHealth());
            }
            AttributeInstance damageAttribute = livingEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            if (damageAttribute != null) {
                damageAttribute.setBaseValue(customMob.getAttackDamage());
            }
            AttributeInstance speedAttribute = livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.setBaseValue(customMob.getMovementSpeed());
            }

            for (Map.Entry<Attribute, Double> entry : customMob.getCustomAttributes().entrySet()) {
                AttributeInstance attributeInstance = livingEntity.getAttribute(entry.getKey());
                if (attributeInstance != null) {
                    attributeInstance.setBaseValue(entry.getValue());
                }
            }

            goalExecutors.put(livingEntity, new CustomMobGoalExecutor(livingEntity, customMob.getGoals()));

            logger.info("Spawned custom mob: " + customMob.getId() + " at " + livingEntity.getLocation());
            return livingEntity;
        } else {
            logger.warning("Custom mob base type " + customMob.getBaseType().name() + " is not a LivingEntity. Cannot apply custom attributes.");
            return entity;
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (container.has(customMobIdKey, PersistentDataType.STRING)) {
            String customMobId = container.get(customMobIdKey, PersistentDataType.STRING);
            CustomMob customMob = getCustomMob(customMobId);
            if (customMob != null) {
            }
        }
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
        HandlerList.unregisterAll(this);
        logger.info("Unregistered all custom mob definitions and event listener.");
    }
}
