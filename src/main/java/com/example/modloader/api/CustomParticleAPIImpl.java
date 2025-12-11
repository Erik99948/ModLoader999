package com.example.modloader.api;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class CustomParticleAPIImpl implements CustomParticleAPI {

    @Override
    public void spawnParticle(World world, Particle particle, Location location, int count) {
        world.spawnParticle(particle, location, count);
    }

    @Override
    public void spawnParticle(World world, Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }

    @Override
    public <T> void spawnParticle(World world, Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double speed, T data) {
        world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, data);
    }
}
