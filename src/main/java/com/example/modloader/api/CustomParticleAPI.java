package com.example.modloader.api;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

/**
 * API for spawning particles.
 */
public interface CustomParticleAPI {
    void spawnParticle(World world, Particle particle, Location location, int count);
    void spawnParticle(World world, Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double speed);
    <T> void spawnParticle(World world, Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double speed, T data);
}
