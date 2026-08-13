package ir.ac.pvz.model.armor;

import ir.ac.pvz.model.support.ArmorDataRepository;

public class ConeArmorDecorator extends ArmorDecorator {
    public ConeArmorDecorator() {
        super(ArmorDataRepository.getInstance().getHealth("ConeDefault"));
    }
}
