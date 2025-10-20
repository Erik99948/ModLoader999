package com.example.modloader.api;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public interface CustomParticleAPI {

    /**
     * Spawns a particle effect at a specific location.
     *
     * @param world The world where the particle should be spawned.
     * @param particle The type of particle to spawn.
     * @param location The location where the particle should be spawned.
     * @param count The number of particles to spawn.
     */
    void spawnParticle(World world, Particle particle, Location location, int count);

    /**
     * Spawns a particle effect at a specific location with additional parameters.
     *
     * @param world The world where the particle should be spawned.
     * @param particle The type of particle to spawn.
     * @param location The location where the particle should be spawned.
     * @param count The number of particles to spawn.
     * @param offsetX The maximum random offset in the X direction.
     * @param offsetY The maximum random offset in the Y direction.
     * @param offsetZ The maximum random offset in the Z direction.
     * @param speed The speed of the particles.
     */
    void spawnParticle(World world, Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double speed);

    /**
     * Spawns a particle effect at a specific location with additional parameters and data.
     *
     * @param world The world where the particle should be spawned.
     * @param particle The type of particle to spawn.
     * @param location The location where the particle should be spawned.
     * @param count The number of particles to spawn.
     * @param offsetX The maximum random offset in the X direction.
     * @param offsetY The maximum random offset in the Y direction.
     * @param offsetZ The maximum random offset in the Z direction.
     * @param speed The speed of the particles.
     * @param data Additional data for the particle (e.g., MaterialData for BLOCK_CRACK, Color for REDSTONE).
     * @param <T> The type of the particle data.
     */
    <T> void spawnParticle(World world, Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double speed, T data);
}