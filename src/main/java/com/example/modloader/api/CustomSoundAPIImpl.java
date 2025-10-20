package com.example.modloader.api;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;

public class CustomSoundAPIImpl implements CustomSoundAPI {

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