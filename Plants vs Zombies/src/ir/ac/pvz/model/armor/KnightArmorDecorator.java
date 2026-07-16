package com.pvz.model.armor;

import com.pvz.model.support.ArmorDataRepository;

public class KnightArmorDecorator extends ArmorDecorator {

    public KnightArmorDecorator() {
        super(ArmorDataRepository.getInstance().getHealth("CrownDefault"));
    }
}
