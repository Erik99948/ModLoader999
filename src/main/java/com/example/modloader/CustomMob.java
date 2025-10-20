package com.example.modloader;

import com.example.modloader.api.mob.CustomMobGoal;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CustomMob {
    private final String id;
    private final EntityType baseType;
    private final String name;
    private final double maxHealth;
    private final double attackDamage;
    private final double movementSpeed;
    private final int customModelData;
    private final List<CustomMobGoal> goals;
    private final Map<Attribute, Double> customAttributes;

    public CustomMob(String id, EntityType baseType, String name, double maxHealth, double attackDamage, double movementSpeed, int customModelData, List<CustomMobGoal> goals, Map<Attribute, Double> customAttributes) {
        this.id = id;
        this.baseType = baseType;
        this.name = name;
        this.maxHealth = maxHealth;
        this.attackDamage = attackDamage;
        this.movementSpeed = movementSpeed;
        this.customModelData = customModelData;
        this.goals = goals != null ? Collections.unmodifiableList(goals) : Collections.emptyList();
        this.customAttributes = customAttributes != null ? Collections.unmodifiableMap(customAttributes) : Collections.emptyMap();
    }

    public String getId() {
        return id;
    }

    public EntityType getBaseType() {
        return baseType;
    }

    public String getName() {
        return name;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getAttackDamage() {
        return attackDamage;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public List<CustomMobGoal> getGoals() {
        return goals;
    }

    public Map<Attribute, Double> getCustomAttributes() {
        return customAttributes;
    }
}
