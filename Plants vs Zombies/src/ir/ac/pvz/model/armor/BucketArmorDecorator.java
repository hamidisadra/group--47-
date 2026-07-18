package ir.ac.pvz.model.armor;

import ir.ac.pvz.model.support.ArmorDataRepository;

public class BucketArmorDecorator extends ArmorDecorator {

    public BucketArmorDecorator() {
        super(ArmorDataRepository.getInstance().getHealth("BucketDefault"));
    }
}
