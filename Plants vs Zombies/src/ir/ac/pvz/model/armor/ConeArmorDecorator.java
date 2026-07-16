package com.pvz.model.armor;

import com.pvz.model.support.ArmorDataRepository;

public class ConeArmorDecorator extends ArmorDecorator {

    public ConeArmorDecorator() {
        super(ArmorDataRepository.getInstance().getHealth("ConeDefault"));
    }
}
