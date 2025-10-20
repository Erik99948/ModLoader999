package com.example.modloader;

import com.example.modloader.api.block.BlockBreakBehavior;
import com.example.modloader.api.block.BlockInteractBehavior;
import com.example.modloader.api.block.BlockPlaceBehavior;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CustomBlockRegistry implements Listener {

    private final Plugin plugin;
    private final Logger logger;
    private final Map<String, CustomBlock> registeredCustomBlocks = new HashMap<>();
    private final NamespacedKey customBlockIdKey;

    public CustomBlockRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.customBlockIdKey = new NamespacedKey(plugin, "custom_block_id");

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
            if (state instanceof org.bukkit.block.TileState) {
                PersistentDataContainer container = ((org.bukkit.block.TileState) state).getPersistentDataContainer();
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
    
                if (state instanceof org.bukkit.block.TileState) {
                    ((org.bukkit.block.TileState) state).getPersistentDataContainer().set(customBlockIdKey, PersistentDataType.STRING, customBlock.getId());
                    state.update(true);
                }
    
                logger.info("Custom block '" + customBlock.getId() + "' placed at " + placedBlock.getLocation());
    
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
            logger.info("Custom block '" + customBlock.getId() + "' broken at " + brokenBlock.getLocation());

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
            logger.info("Player interacted with custom block '" + customBlock.getId() + "' at " + clickedBlock.getLocation());

            BlockInteractBehavior behavior = customBlock.getInteractBehavior();
            if (behavior != null) {
                behavior.onInteract(event, clickedBlock, event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onBlockRedstone(org.bukkit.event.block.BlockRedstoneEvent event) {
        Block block = event.getBlock();
        CustomBlock customBlock = getCustomBlockFromWorldBlock(block);

        if (customBlock != null) {
            com.example.modloader.api.block.BlockRedstoneBehavior behavior = customBlock.getRedstoneBehavior();
            if (behavior != null) {
                behavior.onRedstone(event, block, event.getOldCurrent(), event.getNewCurrent());
            }
        }
    }

    @EventHandler
    public void onBlockExplode(org.bukkit.event.block.BlockExplodeEvent event) {
        List<Block> blocks = new java.util.ArrayList<>(event.blockList());
        for (Block block : blocks) {
            CustomBlock customBlock = getCustomBlockFromWorldBlock(block);
            if (customBlock != null) {
                com.example.modloader.api.block.BlockExplodeBehavior behavior = customBlock.getExplodeBehavior();
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
        HandlerList.unregisterAll(this);
        logger.info("Unregistered all custom block definitions and event listener.");
    }
}
