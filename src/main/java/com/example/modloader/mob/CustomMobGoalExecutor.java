package com.example.modloader.mob;

import com.example.modloader.api.mob.CustomMobGoal;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class CustomMobGoalExecutor {

    private final LivingEntity entity;
    private final List<CustomMobGoal> goals;
    private final List<CustomMobGoal> runningGoals = new ArrayList<>();

    public CustomMobGoalExecutor(LivingEntity entity, List<CustomMobGoal> goals) {
        this.entity = entity;
        this.goals = goals;
    }

    public void tick() {
        for (CustomMobGoal goal : goals) {
            if (!runningGoals.contains(goal) && goal.shouldStart(entity)) {
                goal.start(entity);
                runningGoals.add(goal);
            }
        }

        for (CustomMobGoal goal : new ArrayList<>(runningGoals)) {
            if (!goal.shouldStart(entity)) {
                goal.stop(entity);
                runningGoals.remove(goal);
            } else {
                goal.tick(entity);
            }
        }
    }
}

