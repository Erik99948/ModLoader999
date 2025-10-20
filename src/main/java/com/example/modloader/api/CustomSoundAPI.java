package com.example.modloader.api;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;

public interface CustomSoundAPI {

    /**
     * Plays a sound at a specific location.
     *
     * @param world The world where the sound should be played.
     * @param sound The type of sound to play (from Bukkit's Sound enum).
     * @param location The location where the sound should be played.
     * @param volume The volume of the sound (1.0 is normal).
     * @param pitch The pitch of the sound (1.0 is normal).
     */
    void playSound(World world, Sound sound, Location location, float volume, float pitch);

    /**
     * Plays a sound at a specific location with a specified sound category.
     *
     * @param world The world where the sound should be played.
     * @param sound The type of sound to play (from Bukkit's Sound enum).
     * @param location The location where the sound should be played.
     * @param category The sound category (e.g., MASTER, MUSIC, WEATHER).
     * @param volume The volume of the sound (1.0 is normal).
     * @param pitch The pitch of the sound (1.0 is normal).
     */
    void playSound(World world, Sound sound, Location location, SoundCategory category, float volume, float pitch);

    /**
     * Plays a custom sound at a specific location.
     * This method assumes the custom sound is already defined in the resource pack's sounds.json.
     *
     * @param world The world where the sound should be played.
     * @param soundKey The key of the custom sound (e.g., "modloader:custom_sound").
     * @param location The location where the sound should be played.
     * @param volume The volume of the sound (1.0 is normal).
     * @param pitch The pitch of the sound (1.0 is normal).
     */
    void playCustomSound(World world, String soundKey, Location location, float volume, float pitch);

    /**
     * Plays a custom sound at a specific location with a specified sound category.
     * This method assumes the custom sound is already defined in the resource pack's sounds.json.
     *
     * @param world The world where the sound should be played.
     * @param soundKey The key of the custom sound (e.g., "modloader:custom_sound").
     * @param location The location where the sound should be played.
     * @param category The sound category (e.g., MASTER, MUSIC, WEATHER).
     * @param volume The volume of the sound (1.0 is normal).
     * @param pitch The pitch of the sound (1.0 is normal).
     */
    void playCustomSound(World world, String soundKey, Location location, SoundCategory category, float volume, float pitch);
}