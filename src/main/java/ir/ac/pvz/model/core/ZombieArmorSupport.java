package ir.ac.pvz.model.core;

import ir.ac.pvz.model.support.ArmorPiece;
import ir.ac.pvz.model.support.ZombieAbility;

import java.util.ArrayList;
import java.util.Iterator;

final class ZombieArmorSupport {
    private ZombieArmorSupport() {
    }

    static int absorb(Zombie zombie, int amount) {
        int remaining = amount;
        Iterator<ArmorPiece> iterator = zombie.armorPieces.iterator();

        while (iterator.hasNext() && remaining > 0) {
            ArmorPiece piece = iterator.next();
            remaining = piece.absorbDamage(remaining);

            if (piece.isBroken()) {
                iterator.remove();
            }
        }

        refreshTopPiece(zombie);

        return remaining;
    }

    static void refreshTopPiece(Zombie zombie) {
        if (zombie.armorPieces.isEmpty()) {
            zombie.armor = null;
            return;
        }

        zombie.armor = zombie.armorPieces.get(0);
    }

    static int remainingHealth(Zombie zombie) {
        int total = 0;

        for (ArmorPiece piece : zombie.armorPieces) {
            total += Math.max(0, piece.health);
        }

        return total;
    }

    static boolean blocksAbilityDamage(Zombie zombie, int amount) {
        for (ZombieAbility ability : new ArrayList<>(zombie.abilities)) {
            if (ability.blocksDamage(zombie, amount)) {
                return true;
            }
        }

        return false;
    }

    static void notifyDamaged(Zombie zombie, int armorPiecesBefore) {
        for (ZombieAbility ability : new ArrayList<>(zombie.abilities)) {
            ability.onDamaged(zombie, armorPiecesBefore,
                    zombie.armorPieces.size());
        }
    }
}
