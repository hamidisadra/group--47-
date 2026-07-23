package com.pvz.model.support;

import com.pvz.model.core.Zombie;

public final class NewspaperEnrageAbility extends ZombieAbility {
    private boolean enraged;
    private final float speedScale;
    private final int damageScale;
    public NewspaperEnrageAbility() {
        this(4d, 4d);
    }
    public NewspaperEnrageAbility(double speedScale, double damageScale) {
        super("newspaper-enrage",
                "Speeds up after the newspaper armor is destroyed.", 0f);
        enraged = false;
        this.speedScale = (float) Math.max(1d, speedScale);
        this.damageScale = Math.max(1, (int) Math.round(damageScale));
    }
    @Override
    public void onDamaged(Zombie zombie, int armorPiecesBefore,
                          int armorPiecesAfter) {
        if (!enraged && armorPiecesBefore > 0 && armorPiecesAfter == 0) {
            enraged = true;
            zombie.speed *= speedScale;
            zombie.attackDamage *= damageScale;
            zombie.damageToPlant *= damageScale;
        }
    }
}
