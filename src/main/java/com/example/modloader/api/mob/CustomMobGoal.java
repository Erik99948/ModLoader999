package com.example.modloader.api.mob;

import org.bukkit.entity.LivingEntity;

public interface CustomMobGoal {

    void start(LivingEntity entity);

    void tick(LivingEntity entity);

    void stop(LivingEntity entity);

    boolean shouldStart(LivingEntity entity);
}
