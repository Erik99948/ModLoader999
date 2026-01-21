package com.example.modloader.api.mob;

import org.bukkit.entity.LivingEntity;

public interface CustomMobGoal {
    boolean shouldStart(LivingEntity entity);
    void start(LivingEntity entity);
    void tick(LivingEntity entity);
    void stop(LivingEntity entity);
    int getPriority();
}
