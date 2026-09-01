package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import ir.ac.pvz.controller.gui.PlantCatalogController;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.model.core.Plant;
import ir.ac.pvz.model.enums.PlantCategory;
import ir.ac.pvz.model.enums.PlantTag;
import ir.ac.pvz.model.support.ZombieDataRepository;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.assets.GameAssets;
import ir.ac.pvz.view.ui.PamActor;
import ir.ac.pvz.view.ui.PlantCard;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

public class CollectionScreen extends BaseMenuScreen {
    private final PlantCatalogController catalog = PlantCatalogController.getInstance();
    private final Table grid = new Table();
    private final Table details = new Table();
    private final Table tabs = new Table();
    private SelectBox<String> filterBox;
    private boolean plantsTab = true;
    private String filter = "All";
    private String selectedType;

    public CollectionScreen(PvzGame game) {
        super(game, "Collection", true);

        filterBox = new SelectBox<>(Ui.skin());
        filterBox.setItems(new Array<>(new String[] { "All", "Unlocked", "Locked",
            "Upgradable", "Sun producer", "Shooter", "Lobber", "Explosive", "Melee",
            "Wall", "Modifier", "Strike through", "Homing", "Mint" }));
        filterBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                filter = filterBox.getSelected();
                rebuild();
            }
        });
        content.add(tabs).padBottom(10f).row();

        grid.top();
        ScrollPane pane = new ScrollPane(grid, Ui.skin());
        pane.setFadeScrollBars(false);

        details.top();
        details.setBackground(Ui.panel());
        details.pad(16f);

        Table split = new Table();
        split.add(pane).width(780f).height(430f).padRight(14f).top();
        split.add(details).width(400f).height(430f).top();
        content.add(split).row();

        rebuild();
    }

    private Actor tab(String text, boolean plants) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button =
            Ui.button(text, plants == plantsTab ? "green" : "brown");
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (plantsTab != plants) {
                    plantsTab = plants;
                    selectedType = null;
                    rebuild();
                }
            }
        });
        Table wrapper = new Table();
        wrapper.add(button).width(180f).height(56f);
        return wrapper;
    }

    private void rebuild() {
        grid.clear();
        details.clear();
        tabs.clear();
        tabs.add(tab("Plants", true)).pad(4f);
        tabs.add(tab("Zombies", false)).pad(4f);
        if (plantsTab) {
            tabs.add(filterBox).width(220f).height(48f).padLeft(20f);
        }

        if (plantsTab) {
            buildPlants();
        }

        else {
            buildZombies();
        }
    }

    private void buildPlants() {
        int column = 0;
        for (String type : catalog.allTypes()) {
            if (!matchesFilter(type)) {
                continue;
            }

            boolean unlocked = catalog.isUnlocked(type);
            PlantCard card = new PlantCard(type, 118f);
            Plant prototype = catalog.prototype(type);
            card.cost(prototype == null ? 0 : prototype.sunCost)
                .level(catalog.level(type))
                .locked(!unlocked)
                .boosted(catalog.isBoosted(type))
                .seedProgress(catalog.seedPackets(type),
                    catalog.seedPacketsForNextLevel(type))
                .selected(type.equals(selectedType));
            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectedType = type;
                    rebuild();
                }
            });
            if (catalog.isPremium(type)) {
                Table wrapper = new Table();
                wrapper.add(card).row();
                wrapper.add(premiumBadge()).width(118f);
                grid.add(wrapper).pad(6f);
            }

            else {
                grid.add(card).pad(6f);
            }

            column++;
            if (column % 6 == 0) {
                grid.row();
            }
        }

        if (selectedType != null) {
            showPlantDetails(selectedType);
        }

        else {
            details.add(Ui.label("Select a plant to see its details.", "secondary"))
                .width(360f).row();
        }
    }

    private boolean matchesFilter(String type) {
        boolean unlocked = catalog.isUnlocked(type);

        if (filter.equals("All")) {
            return true;
        }

        if (filter.equals("Unlocked")) {
            return unlocked;
        }

        if (filter.equals("Locked")) {
            return !unlocked;
        }

        if (filter.equals("Upgradable")) {
            return catalog.canUpgrade(type);
        }

        PlantCategory category = catalog.family(type);
        return category != null && category == categoryOf(filter);
    }

    private PlantCategory categoryOf(String label) {
        switch (label) {
            case "Sun producer":
                return PlantCategory.SUN_PRODUCER;

            case "Shooter":
                return PlantCategory.SHOOTER;

            case "Lobber":
                return PlantCategory.LOBBER;

            case "Explosive":
                return PlantCategory.EXPLOSIVE;

            case "Melee":
                return PlantCategory.MELEE;

            case "Wall":
                return PlantCategory.WALL;

            case "Modifier":
                return PlantCategory.MODIFIER;

            case "Strike through":
                return PlantCategory.STRIKE_THROUGH;

            case "Homing":
                return PlantCategory.HOMING;

            case "Mint":
                return PlantCategory.MINT;

            default:
                return null;
        }
    }

    private void showPlantDetails(String type) {
        Plant plant = catalog.prototype(type);
        if (plant == null) {
            return;
        }

        PamActor art = new PamActor(150f);
        art.setAnimation(GameAssets.get().plantPath(type), "idle");
        details.add(art).size(150f).padBottom(8f).row();

        Label name = Ui.label(type, "medium");
        name.setAlignment(Align.center);
        details.add(name).width(360f).padBottom(10f).row();

        addPlantStats(type, plant);
        details.add(plantActions(type)).padTop(10f).row();
    }

    private void addPlantStats(String type, Plant plant) {
        addStat("Health", String.valueOf(plant.baseHp));
        addStat("Sun cost", String.valueOf(plant.sunCost));
        addStat("Damage", String.valueOf(plant.attackPower));
        addStat("Recharge", plant.rechargeTime + "s");
        addStat("Family", plant.category == null ? "-" : plant.category.name());
        addStat("Level", String.valueOf(catalog.level(type)));
        addStat("Seed packets", catalog.seedPackets(type) + " / "
            + catalog.seedPacketsForNextLevel(type));

        StringBuilder tags = new StringBuilder();
        for (PlantTag tag : plant.plantTags) {
            if (tags.length() > 0) {
                tags.append(", ");
            }

            tags.append(tag.name());
        }

        addStat("Tags", tags.length() == 0 ? "-" : tags.toString());
    }

    private Table plantActions(String type) {
        Table actions = new Table();

        if (!catalog.isUnlocked(type)) {
            boolean premium = catalog.isPremium(type);
            String label = premium
                ? "Unlock premium (" + PlantCatalogController.UNLOCK_COST_GEMS + " gems)"
                : "Unlock (" + PlantCatalogController.UNLOCK_COST_COINS + " coins)";
            actions.add(actionButton(label, premium ? "purple" : "green",
                () -> runCatalogAction(catalog.unlock(type), type + " unlocked.")))
                .pad(4f).row();
            return actions;
        }

        actions.add(actionButton("Upgrade", "green",
            () -> runCatalogAction(catalog.upgrade(type), type + " upgraded.")))
            .pad(4f).row();
        return actions;
    }

    private void runCatalogAction(String error, String successMessage) {
        if (error != null) {
            Toast.error(stage, error);
            return;
        }

        Toast.info(stage, successMessage);
        rebuild();
    }

    private Actor actionButton(String text, String style, Runnable onClick) {
        com.badlogic.gdx.scenes.scene2d.ui.TextButton button = Ui.button(text, style);
        button.getLabel().setFontScale(0.85f);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        Table wrapper = new Table();
        wrapper.add(button).width(330f).height(56f);
        return wrapper;
    }

    private void addStat(String title, String value) {
        Table row = new Table();
        row.add(Ui.label(title, "secondary")).width(170f).left();
        row.add(Ui.label(value, "secondary")).width(180f).left();
        details.add(row).padBottom(4f).row();
    }

    private void buildZombies() {
        User user = MenuManager.getInstance().getActiveUser();
        java.util.List<String> seen = user == null
            ? new java.util.ArrayList<>() : user.getCollection().getSeenZombies();

        int column = 0;
        for (ir.ac.pvz.model.support.ZombieDefinition definition
            : ZombieDataRepository.getInstance().getAll()) {
            grid.add(zombieCard(definition.gameType,
                seen.contains(definition.gameType))).width(126f).pad(6f);
            column++;
            if (column % 6 == 0) {
                grid.row();
            }
        }

        if (selectedType != null && seen.contains(selectedType)) {
            showZombieDetails(selectedType);
        }

        else {
            details.add(Ui.label("Select a discovered zombie.", "secondary"))
                .width(360f).row();
        }
    }

    private Table zombieCard(String type, boolean discovered) {
        Table card = new Table();
        card.setBackground(Ui.panel());
        card.pad(8f);

        if (!discovered) {
            card.add(Ui.label("?", "big")).size(84f).row();
            card.add(Ui.label("undiscovered", "secondary")).width(110f).row();
            return card;
        }

        PamActor art = new PamActor(84f);
        art.setAnimation(GameAssets.get().zombiePath(type,
            ir.ac.pvz.model.enums.SeasonType.ANCIENT_EGYPT), "idle", "walk");
        card.add(art).size(84f).row();

        Label name = Ui.label(type, "secondary");
        name.setFontScale(0.7f);
        name.setAlignment(Align.center);
        card.add(name).width(110f).row();

        if (PlantCatalogController.isPremiumZombie(type)) {
            card.add(premiumBadge()).width(110f).row();
        }

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedType = type;
                rebuild();
            }
        });

        return card;
    }

    private Label premiumBadge() {
        Label badge = Ui.label("PREMIUM", "secondary");
        badge.setFontScale(0.6f);
        badge.setAlignment(Align.center);
        badge.setColor(1f, 0.82f, 0.25f, 1f);
        return badge;
    }

    private void showZombieDetails(String type) {
        details.clear();
        PamActor art = new PamActor(150f);
        art.setAnimation(GameAssets.get().zombiePath(type,
            ir.ac.pvz.model.enums.SeasonType.ANCIENT_EGYPT), "idle", "walk");
        details.add(art).size(150f).padBottom(8f).row();
        Label name = Ui.label(type, "medium");
        name.setAlignment(Align.center);
        details.add(name).width(360f).padBottom(10f).row();

        ir.ac.pvz.model.support.ZombieDefinition definition =
            ZombieDataRepository.getInstance().getByZombieType(type);
        if (definition != null) {
            addStat("Health", String.valueOf(definition.health));
            addStat("Speed", String.valueOf(definition.speed));
            addStat("Damage per second", String.valueOf(definition.eatDamagePerSecond));
            addStat("Wave cost", String.valueOf(definition.waveCost));
            addStat("Drops plant food", definition.canSpawnPlantFood ? "yes" : "no");
        }
    }
}
