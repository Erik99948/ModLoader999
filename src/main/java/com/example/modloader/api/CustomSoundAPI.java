package com.example.modloader.api;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;

public interface CustomSoundAPI {

    void playSound(World world, Sound sound, Location location, float volume, float pitch);

    void playSound(World world, Sound sound, Location location, SoundCategory category, float volume, float pitch);

    void playCustomSound(World world, String soundKey, Location location, float volume, float pitch);

    void playCustomSound(World world, String soundKey, Location location, SoundCategory category, float volume, float pitch);
}