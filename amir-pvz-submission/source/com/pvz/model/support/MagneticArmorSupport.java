package com.pvz.model.support;

import com.pvz.model.core.Plant;
import com.pvz.model.core.Zombie;
import java.util.Iterator;
import java.util.List;


final class MagneticArmorSupport {
    private MagneticArmorSupport() {
    }
    static boolean removeNearest(Plant plant, List<Zombie> zombies) {
        Zombie nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Zombie zombie : zombies) {
            if (!hasMagneticArmor(zombie)
                    || !ProjectilePlantSupport.isWithinAttackRange(
                    plant, zombie)) {
                continue;
            }
            double distance = ProjectileTargetSupport.distance(
                    plant.location, zombie.currentPosition);
            if (distance < nearestDistance) {
                nearest = zombie;
                nearestDistance = distance;
            }
        }
        if (nearest == null) {
            return false;
        }
        removeOne(nearest);
        return true;
    }
    private static boolean hasMagneticArmor(Zombie zombie) {
        for (ArmorPiece piece : zombie.armorPieces) {
            if (piece.magnetic) {
                return true;
            }
        }
        return false;
    }
    private static void removeOne(Zombie zombie) {
        Iterator<ArmorPiece> iterator = zombie.armorPieces.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().magnetic) {
                iterator.remove();
                updateActiveArmor(zombie);
                return;
            }
        }
    }
    private static void updateActiveArmor(Zombie zombie) {
        if (zombie.armorPieces.isEmpty()) {
            zombie.armor = null;
        }
        else {
            zombie.armor = zombie.armorPieces.get(0);
        }
    }
}
