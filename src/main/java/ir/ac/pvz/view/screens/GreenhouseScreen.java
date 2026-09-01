package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.user.GreenHouse;
import ir.ac.pvz.model.user.HarvestResult;
import ir.ac.pvz.model.user.Pot;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.model.support.PlantDataRepository;
import ir.ac.pvz.model.support.PlantDefinition;
import java.util.ArrayList;
import java.util.List;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.assets.GameAssets;
import ir.ac.pvz.view.ui.Modal;
import ir.ac.pvz.view.ui.PamActor;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

public class GreenhouseScreen extends BaseMenuScreen {
    private static final int POT_COST_COINS = 2000;

    private final Table grid = new Table();
    private final User user;

    public GreenhouseScreen(PvzGame game) {
        super(game, "Greenhouse", true, "IMAGE_GREENHOUSE_BACKGROUND");
        user = MenuManager.getInstance().getActiveUser();
        content.add(grid).center();
        rebuild();
    }

    private void rebuild() {
        grid.clear();
        if (user == null) {
            grid.add(Ui.label("You are not signed in.", "medium"));
            return;
        }

        GreenHouse greenHouse = user.getGreenHouse();
        for (int row = 1; row <= greenHouse.getRows(); row++) {
            for (int column = 1; column <= greenHouse.getColumns(); column++) {
                Pot pot = greenHouse.getPot(column, row);
                grid.add(potCard(pot)).width(196f).pad(9f);
            }

            grid.row();
        }
    }

    private Table potCard(Pot pot) {
        Table card = new Table();
        card.setBackground(Ui.panel());
        card.pad(9f);

        if (pot == null) {
            card.add(Ui.label("-", "medium")).size(96f).row();
            return card;
        }

        if (pot.isLocked()) {
            return lockedCard(card, pot);
        }

        if (pot.isEmpty()) {
            card.add(Ui.label("Empty", "medium")).height(96f).row();
            card.add(button("Plant seed", "green_small", () -> plantSeed(pot)))
                .width(150f).height(46f).row();
            return card;
        }

        return growingCard(card, pot);
    }

    private Table lockedCard(Table card, Pot pot) {
        card.add(Ui.label("Locked", "medium")).height(96f).row();
        card.add(button("Buy pot", "green_small", () -> buyPot(pot)))
            .width(150f).height(46f).row();
        return card;
    }

    private void buyPot(Pot pot) {
        if (user.getWallet().getCoins() < POT_COST_COINS) {
            Toast.error(stage, "A new pot costs " + POT_COST_COINS + " coins.");
            return;
        }

        user.getWallet().spendCoins(POT_COST_COINS);
        user.getGreenHouse().unlockPot(pot.getX(), pot.getY());
        UserManager.getInstance().saveAll();
        Toast.info(stage, "Pot purchased.");
        rebuild();
    }

    private Table growingCard(Table card, Pot pot) {
        PamActor art = new PamActor(92f);
        art.setAnimation(GameAssets.get().plantPath(pot.getPlantType()), "idle");
        card.add(art).size(92f).row();

        Label name = Ui.label(pot.getPlantType(), "secondary");
        name.setFontScale(0.75f);
        name.setAlignment(Align.center);
        card.add(name).width(150f).row();

        if (pot.isReady()) {
            card.add(Ui.label("ready", "secondary")).row();
            card.add(button("Harvest", "green_small", () -> harvest(pot)))
                .width(150f).height(46f).row();
            return card;
        }

        long minutes = Math.round(pot.getRemainingHours() * 60);
        int gems = speedUpCost(pot);
        card.add(Ui.label(minutes + " min left", "secondary")).row();
        card.add(button("Speed up (" + gems + " gems)", "purple",
            () -> speedUp(pot, gems))).width(150f).height(44f).row();
        return card;
    }

    private void speedUp(Pot pot, int gems) {
        if (user.getWallet().getGems() < gems) {
            Toast.error(stage, "You need " + gems + " gems.");
            return;
        }

        user.getWallet().spendGems(gems);
        pot.growInstantly();
        UserManager.getInstance().saveAll();
        rebuild();
    }

    private void plantSeed(Pot pot) {
        try {
            user.getGreenHouse().plantRandom(pot, plantFoodCapablePlants());
        }

        catch (RuntimeException error) {
            Toast.error(stage, "This pot cannot be planted right now.");
            return;
        }

        UserManager.getInstance().saveAll();
        Toast.info(stage, "A seed was planted.");
        rebuild();
    }

    private List<String> plantFoodCapablePlants() {
        List<String> capable = new ArrayList<>();

        for (String plant : user.getCollection().getUnlockedPlants()) {
            PlantDefinition definition = PlantDataRepository.getInstance().get(plant);

            if (definition != null && definition.plantFoodEffect != null
                && !definition.plantFoodEffect.isBlank()) {
                capable.add(plant);
            }
        }

        return capable;
    }

    private int speedUpCost(Pot pot) {
        return Math.max(1, (int) Math.ceil(pot.getRemainingHours()));
    }

    private void harvest(Pot pot) {
        HarvestResult result;

        try {
            result = pot.harvest();
        }

        catch (RuntimeException error) {
            Toast.error(stage, "This plant is not ready yet.");
            return;
        }

        user.getWallet().addCoins(result.getCoins());
        user.getQuestLog().advance("day-greenhouse", 1);
        if (result.getBoostedPlant() != null) {
            user.getCollection().earnPlantBoost(result.getBoostedPlant());
        }

        UserManager.getInstance().saveAll();

        Modal modal = new Modal("Harvest");
        StringBuilder text = new StringBuilder();
        text.append("You collected ").append(result.getCoins()).append(" coins.");
        if (result.getBoostedPlant() != null) {
            text.append("\n").append(result.getBoostedPlant())
                .append(" will be boosted the next time you use it.");
        }

        modal.message(text.toString());
        modal.action("Nice", "green", () -> {
            modal.close();
            rebuild();
        });
        modal.show(stage);
    }

    private Actor button(String text, String style, Runnable onClick) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button(text, style);
        button.getLabel().setFontScale(0.78f);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        return button;
    }
}
