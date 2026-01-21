package com.example.modloader.api.event;

import com.example.modloader.ModInfo;
import org.bukkit.inventory.ItemStack;

/**
 * Contains all mod-related event classes.
 */
public class ModEvents {

    // ==================== Mod Lifecycle Events ====================

    public static class ModPreLoadEvent extends EventBus.BaseEvent {
        private final ModInfo modInfo;

        public ModPreLoadEvent(ModInfo modInfo) {
            this.modInfo = modInfo;
        }

        public ModInfo getModInfo() {
            return modInfo;
        }
    }

    public static class ModPostLoadEvent extends EventBus.BaseEvent {
        private final ModInfo modInfo;

        public ModPostLoadEvent(ModInfo modInfo) {
            this.modInfo = modInfo;
        }

        public ModInfo getModInfo() {
            return modInfo;
        }
    }

    public static class ModPreEnableEvent extends EventBus.BaseEvent {
        private final ModInfo modInfo;

        public ModPreEnableEvent(ModInfo modInfo) {
            this.modInfo = modInfo;
        }

        public ModInfo getModInfo() {
            return modInfo;
        }
    }

    public static class ModPostEnableEvent extends EventBus.BaseEvent {
        private final ModInfo modInfo;

        public ModPostEnableEvent(ModInfo modInfo) {
            this.modInfo = modInfo;
        }

        public ModInfo getModInfo() {
            return modInfo;
        }
    }

    public static class ModPreDisableEvent extends EventBus.BaseEvent {
        private final ModInfo modInfo;

        public ModPreDisableEvent(ModInfo modInfo) {
            this.modInfo = modInfo;
        }

        public ModInfo getModInfo() {
            return modInfo;
        }
    }

    public static class ModPostDisableEvent extends EventBus.BaseEvent {
        private final ModInfo modInfo;

        public ModPostDisableEvent(ModInfo modInfo) {
            this.modInfo = modInfo;
        }

        public ModInfo getModInfo() {
            return modInfo;
        }
    }

    // ==================== Registration Events ====================

    public static class PreRegisterBlockEvent extends EventBus.CancellableEvent {
        private final Object block;

        public PreRegisterBlockEvent(Object block) {
            this.block = block;
        }

        public Object getBlock() {
            return block;
        }
    }

    public static class PreRegisterItemEvent extends EventBus.CancellableEvent {
        private final String itemId;
        private final ItemStack item;

        public PreRegisterItemEvent(String itemId, ItemStack item) {
            this.itemId = itemId;
            this.item = item;
        }

        public String getItemId() {
            return itemId;
        }

        public ItemStack getItem() {
            return item;
        }
    }

    public static class PreRegisterMobEvent extends EventBus.CancellableEvent {
        private final Object mob;

        public PreRegisterMobEvent(Object mob) {
            this.mob = mob;
        }

        public Object getMob() {
            return mob;
        }
    }
}
