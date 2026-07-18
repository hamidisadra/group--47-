package ir.ac.pvz.model.plants;

import ir.ac.pvz.model.enums.PlantTag;

public class IcebergLettuce extends ExplosivePlant {
    public IcebergLettuce(int id) {
        super(id, "Iceberg Lettuce", 0, 300, 20.0f, 0.0f, 0, 0.0f, false, PlantTag.TRAP, PlantTag.ICE);
    }
}
