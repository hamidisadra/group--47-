package ir.ac.pvz.controller.gui;

import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.support.GridPosition;

final class PlantPlacement {
    private PlantPlacement() {
    }

    static boolean plant(SessionController controller, String type,
                         GridPosition position) {
        if (controller.isBowling()) {
            boolean launched = controller.launchNut(type, position);
            if (launched) {
                controller.conveyorQueue.remove(type);
            }

            return launched;
        }

        if (controller.usesConveyor()) {
            if (!controller.conveyorQueue.contains(type)) {
                return false;
            }

            Plant prototype = Plant.createSpreadsheetPlant(0, type);
            int cost = prototype == null ? 0 : prototype.sunCost;
            controller.session.getSunManager().addSuns(cost);
            boolean planted = controller.session.plantPlant(type, position);
            if (planted) {
                controller.conveyorQueue.remove(type);
                if (controller.minigameController != null
                    && controller.minigameController.isVasebreaker()) {
                    controller.minigameController.consumeHarvestedPlant(type);
                }
            }

            else {
                controller.session.getSunManager().spendSuns(cost);
            }

            return planted;
        }

        return controller.session.plantPlant(type, position);
    }

}
