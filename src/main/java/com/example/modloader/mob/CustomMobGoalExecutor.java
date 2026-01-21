package com.example.modloader.mob;

import com.example.modloader.api.mob.CustomMobGoal;
import org.bukkit.entity.LivingEntity;
import java.util.*;

/**
 * Executor for custom mob goals. Manages and ticks mob AI goals.
 */
public class CustomMobGoalExecutor {
    private final LivingEntity entity;
    private final List<CustomMobGoal> goals;
    private final Set<CustomMobGoal> runningGoals = new HashSet<>();

    public CustomMobGoalExecutor(LivingEntity entity, List<CustomMobGoal> goals) {
        this.entity = entity;
        this.goals = goals != null ? new ArrayList<>(goals) : new ArrayList<>();
        this.goals.sort(Comparator.comparingInt(CustomMobGoal::getPriority));
    }

    public void tick() {
        if (entity == null || entity.isDead()) {
            return;
        }

        for (CustomMobGoal goal : goals) {
            boolean shouldRun = goal.shouldStart(entity);
            boolean isRunning = runningGoals.contains(goal);

            if (shouldRun && !isRunning) {
                goal.start(entity);
                runningGoals.add(goal);
            } else if (!shouldRun && isRunning) {
                goal.stop(entity);
                runningGoals.remove(goal);
            }

            if (runningGoals.contains(goal)) {
                goal.tick(entity);
            }
        }
    }

    public void stopAll() {
        for (CustomMobGoal goal : runningGoals) {
            goal.stop(entity);
        }
        runningGoals.clear();
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public List<CustomMobGoal> getGoals() {
        return Collections.unmodifiableList(goals);
    }

    public Set<CustomMobGoal> getRunningGoals() {
        return Collections.unmodifiableSet(runningGoals);
    }
    
    public void addGoal(CustomMobGoal goal) {
        goals.add(goal);
        goals.sort(Comparator.comparingInt(CustomMobGoal::getPriority));
    }
    
    public void removeGoal(CustomMobGoal goal) {
        if (runningGoals.contains(goal)) {
            goal.stop(entity);
            runningGoals.remove(goal);
        }
        goals.remove(goal);
    }
}
