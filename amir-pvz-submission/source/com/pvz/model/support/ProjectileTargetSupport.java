package com.pvz.model.support;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.random.RandomGenerator;

final class ProjectileTargetSupport {
    private ProjectileTargetSupport() {
    }
    static void freezeAllZombies(Board board, int ticks) {
        for (Zombie zombie : board.getAllAliveZombies()) {
            zombie.freeze(ticks);
        }
    }
    static void damageAreaAroundZombie(
            Zombie center, int damage, int radius,
            Board board, Zombie excluded) {
        GridPosition position = new GridPosition(
                (int) Math.floor(center.currentPosition.x), center.lane);
        for (Zombie zombie : board.getZombiesAround(position, radius)) {
            if (zombie != excluded) {
                zombie.takeDamage(damage);
            }
        }
    }
    static void resetSamePlantAges(Plant source, Board board) {
        for (int row = 0; row < board.rows; row++) {
            for (Plant plant : board.getPlantsInLane(row)) {
                if (plant.getNormalizedType().equals(
                        source.getNormalizedType())) {
                    plant.resetAge();
                }
            }
        }
    }
    static int kiwibeastDamage(Plant plant) {
        if (plant.level >= 4 && plant.getAgeSeconds() >= 120f) {
            return 60;
        }
        if (plant.getAgeSeconds() >= 72f) {
            return 45;
        }
        if (plant.getAgeSeconds() >= 24f) {
            return 30;
        }
        return 15;
    }
    static Zombie nearestAhead(Plant plant, Board board) {
        Zombie target = board.getNearestZombieAhead(
                plant.location.x, plant.location.y);
        if (target == null
                || !ProjectilePlantSupport.isWithinAttackRange(plant, target)) {
            return null;
        }
        return target;
    }
    static List<Zombie> aliveAheadInLane(Plant plant, Board board) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : board.getZombiesInLane(plant.location.y)) {
            if (!zombie.isDead()
                    && zombie.currentPosition.x >= plant.location.x
                    && ProjectilePlantSupport.isWithinAttackRange(
                    plant, zombie)) {
                result.add(zombie);
            }
        }
        result.sort(Comparator.comparingDouble(
                zombie -> zombie.currentPosition.x));
        return result;
    }
    static List<Zombie> zombiesFrontAndBack(
            Plant plant, int range, Board board) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : board.getZombiesInLane(plant.location.y)) {
            double offset = Math.abs(
                    zombie.currentPosition.x - plant.location.x);
            if (!zombie.isDead() && offset <= range) {
                result.add(zombie);
            }
        }
        return result;
    }
    static List<Zombie> randomTargets(
            List<Zombie> source, int count, RandomGenerator random) {
        List<Zombie> copy = new ArrayList<>(source);
        for (int index = copy.size() - 1; index > 0; index--) {
            int selected = random.nextInt(index + 1);
            Zombie temporary = copy.get(index);
            copy.set(index, copy.get(selected));
            copy.set(selected, temporary);
        }
        int resultSize = Math.min(Math.max(0, count), copy.size());
        return new ArrayList<>(copy.subList(0, resultSize));
    }
    static Zombie chooseHomingTarget(
            Plant plant, List<Zombie> zombies, RandomGenerator random) {
        if (plant.getNormalizedType().equals("electricblueberry")
                && plant.level >= 3) {
            return zombies.stream().max(Comparator.comparingInt(
                    zombie -> zombie.currentHealth)).orElse(zombies.get(0));
        }
        if (plant.getNormalizedType().equals("caulipower")) {
            return zombies.get(random.nextInt(zombies.size()));
        }
        return zombies.stream().min(Comparator.comparingDouble(zombie ->
                        distance(plant.location, zombie.currentPosition)))
                .orElse(zombies.get(0));
    }
    static double distance(
            GridPosition plant, ContinuousPosition zombie) {
        double dx = plant.x - zombie.x;
        double dy = plant.y - zombie.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
