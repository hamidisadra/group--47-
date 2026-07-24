package com.pvz.model.armor;

import com.pvz.model.support.ArmorDataRepository;

public class BucketArmorDecorator extends ArmorDecorator {
    public BucketArmorDecorator() {
        super(ArmorDataRepository.getInstance().getHealth("BucketDefault"));
    }
}
