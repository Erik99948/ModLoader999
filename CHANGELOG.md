# Changelog

## 1.1.0 - 2025-10-19

### Added

*   **Asset Validation:**
    *   Automatic syntax validation for `.json` model files during resource pack generation.
    *   Support for copying `.ogg` sound files into the resource pack.
*   **Custom GUI/Inventory API:**
    *   API for creating custom inventories (`Inventory`).
    *   Methods for setting/getting items in custom inventories.
    *   Handlers for `InventoryClickEvent` and `InventoryCloseEvent` for custom GUIs.
*   **Custom Particle API:**
    *   API for spawning custom particle effects (`Particle`).
    *   Support for various particle parameters (count, offset, speed, data).
*   **Custom Sound API:**
    *   API for playing vanilla (`Sound`) and custom sounds.
    *   Support for `SoundCategory` and custom sound keys.
    *   Automatic inclusion of `sounds.json` and `.ogg` files into the resource pack.
*   **Custom Enchantment API:**
    *   API for defining and registering custom enchantments (`CustomEnchantment`).
    *   Reflection-based registration to integrate with Bukkit's enchantment system.
    *   Examples for defining conflicts and item targets.
*   **Custom Potion Effect API:**
    *   API for defining and registering custom potion effect types (`CustomPotionEffectType`).
    *   Reflection-based registration to integrate with Bukkit's potion effect system.
    *   Methods for applying custom potion effects to `LivingEntity` instances.
*   **Custom World Generation Enhancements:**
    *   **Biome-Specific Generators:** World generation methods (populators, ore, tree, structure generators) now accept optional `Biome... biomes` parameters for biome-specific generation.
    *   **Advanced Custom Chunk Generators:** API for creating custom `ChunkGenerator` implementations (`CustomChunkGenerator`) for full control over chunk terrain and block composition.
    *   **Dimension Management:** API for creating, loading, unloading, and getting worlds/dimensions (`DimensionAPI`).
*   **Custom Structure Management API:**
    *   API for loading and spawning custom structures (`CustomStructureAPI`).
    *   Placeholder implementation for loading structures from files (e.g., `.nbt`).
    *   Methods for spawning structures at a `Location` with options for rotation, mirroring, and integrity (currently placeholder).

### Changed

*   `ResourcePackGenerator`: Now copies `.json` and `.ogg` files, and performs basic JSON validation.
*   `CustomWorldGeneratorRegistry`: Refactored to support world-specific and biome-specific generator registration using named inner classes for `BlockPopulator` implementations.
*   `ModAPI` and `ModAPIImpl`: Updated to include new API getters and modified world generation registration methods.
*   `ModLoaderService`: Updated to unregister new API components on plugin disable.
*   `ModLoader`: Updated to register `CustomInventoryListener`.

### Fixed

*   Resolved compilation errors related to `Enchantment` and `PotionEffectType` constructors by using reflection-based key setting.
*   Resolved `cannot find symbol` errors for `chunk.getBiome()` by ensuring correct method signature usage.
*   Resolved `ModAPIImpl` override errors by ensuring all `ModAPI` methods are correctly implemented.
*   Fixed syntax error in `ResourcePackGenerator.java`'s `copyFolder` method.
