package com.pvz.model.plants;

import com.pvz.model.core.GameObject;
import com.pvz.model.core.Zombie;
import com.pvz.model.enums.PlantTag;

public class WinterMelon extends LobberPlant {
    public WinterMelon(int id) {
        super(id, "Winter Melon", 500, 300, 5f, 2.9f, 80, 1f,
                PlantTag.ICE, PlantTag.AOE);
    }
    @Override
    public void attack(GameObject target) {
        super.attack(target);
        if (target instanceof Zombie) {
            float chillDuration = 3f;
            if (level >= 3) {
                chillDuration = 5f;
            }
            ((Zombie) target).chill(0.5f, chillDuration);
        }
    }
}
