package ir.ac.pvz.model.armor;

import ir.ac.pvz.model.support.ArmorDataRepository;

public class KnightArmorDecorator extends ArmorDecorator {
    public KnightArmorDecorator() {
        super(ArmorDataRepository.getInstance().getHealth("CrownDefault"));
    }
}
