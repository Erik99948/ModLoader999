package com.example.modloader.api.mob;

import org.bukkit.entity.LivingEntity;

public abstract class AbstractCustomMobGoal implements CustomMobGoal {
    protected final int priority;
    protected boolean isRunning = false;

    public AbstractCustomMobGoal(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void start(LivingEntity entity) {
        isRunning = true;
    }

    @Override
    public void stop(LivingEntity entity) {
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
