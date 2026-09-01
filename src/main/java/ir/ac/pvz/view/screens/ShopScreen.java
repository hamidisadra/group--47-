package ir.ac.pvz.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;
import ir.ac.pvz.controller.managers.GameplayManager;
import ir.ac.pvz.controller.managers.MenuManager;
import ir.ac.pvz.controller.managers.UserManager;
import ir.ac.pvz.model.shop.DailyOffer;
import ir.ac.pvz.model.shop.Shop;
import ir.ac.pvz.model.shop.ShopItem;
import ir.ac.pvz.model.shop.ShopResult;
import ir.ac.pvz.model.user.User;
import ir.ac.pvz.view.PvzGame;
import ir.ac.pvz.view.ui.Modal;
import ir.ac.pvz.view.assets.GameAssets;
import ir.ac.pvz.view.ui.PamActor;
import ir.ac.pvz.view.ui.Toast;
import ir.ac.pvz.view.ui.Ui;

public class ShopScreen extends BaseMenuScreen {
    private static final int DAILY_PACKETS = 10;
    private final Shop shop;
    private final User user;
    private final Table list = new Table();

    public ShopScreen(PvzGame game) {
        super(game, "Shop", true);
        shop = GameplayManager.getInstance().getShop();
        user = MenuManager.getInstance().getActiveUser();

        list.top();
        ScrollPane pane = new ScrollPane(list, Ui.skin());
        pane.setFadeScrollBars(false);
        content.add(pane).width(1000f).grow();
        rebuild();
    }

    private void rebuild() {
        list.clear();
        if (user == null) {
            list.add(Ui.label("You are not signed in.", "medium"));
            return;
        }

        shop.getDailyOffer().refreshIfNeeded(user.getCollection().getUnlockedPlants());
        DailyOffer offer = shop.getDailyOffer();
        if (offer.getPlantType() != null) {
            list.add(dailyCard(offer)).width(940f).padBottom(14f).row();
        }

        for (ShopItem item : shop.getPermanentItems()) {
            list.add(itemCard(item)).width(940f).padBottom(12f).row();
        }
    }

    private Table dailyCard(DailyOffer offer) {
        Table card = new Table();
        card.setBackground(Ui.panel());
        card.pad(16f);

        PamActor art = new PamActor(96f);
        art.setAnimation(GameAssets.get().plantPath(offer.getPlantType()), "idle");
        card.add(art).size(96f).padRight(18f);

        Table info = new Table();
        info.add(Ui.label("Daily offer", "medium")).left().row();
        info.add(Ui.label(offer.getPlantType() + "  x" + DAILY_PACKETS + " seed packets",
            "secondary")).left().row();
        info.add(Ui.label(offer.getPriceCoins() + " coins  (20% off)", "secondary")).left().row();
        card.add(info).growX().left();

        com.badlogic.gdx.scenes.scene2d.ui.TextButton buy =
            Ui.button(offer.isAvailableToday() ? "Buy" : "Bought today", "green");
        buy.setDisabled(!offer.isAvailableToday());
        buy.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!offer.isAvailableToday()) {
                    Toast.error(stage, "You already bought today's offer.");
                    return;
                }

                confirm("Buy " + offer.getPlantType() + " packets for "
                    + offer.getPriceCoins() + " coins?", () -> {
                    ShopResult result = shop.buyDailyOffer(user.getWallet(),
                        user.getInventory());
                    report(result);
                });
            }
        });
        card.add(buy).width(200f).height(60f).right();
        return card;
    }

    private Table itemCard(ShopItem item) {
        Table card = new Table();
        card.setBackground(Ui.panel());
        card.pad(16f);

        Table info = new Table();
        Label name = Ui.label(item.getName(), "medium");
        info.add(name).left().row();
        String price = item.getPriceGems() > 0
            ? item.getPriceGems() + " gems" : item.getPriceCoins() + " coins";
        info.add(Ui.label(price + "  for " + item.getBuyUnit(), "secondary")).left().row();
        card.add(info).growX().left();

        com.badlogic.gdx.scenes.scene2d.ui.TextButton buy = Ui.button("Buy", "green");
        buy.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (item.getId().equals("selective-seed")) {
                    chooseSeedPlant(item);
                    return;
                }

                confirm("Buy " + item.getName() + " for " + price + "?",
                    () -> buyPermanent(item));
            }
        });
        card.add(buy).width(200f).height(60f).right();
        return card;
    }

    private void buyPermanent(ShopItem item) {
        Map<String, Integer> before = seedPacketSnapshot();

        ShopResult result = shop.buyItem(item.getId(), 1, null, user.getWallet(),
            user.getGreenHouse(), user.getCollection(), user.getInventory());

        report(result);

        if (result == ShopResult.SUCCESS && item.getId().equals("random-seed")) {
            String granted = grantedPlant(before);

            if (granted != null) {
                Toast.info(stage, "You received " + item.getBuyUnit()
                    + " " + granted + " seed packets.");
            }
        }
    }

    private Map<String, Integer> seedPacketSnapshot() {
        Map<String, Integer> counts = new HashMap<>();

        for (String plant : user.getCollection().getUnlockedPlants()) {
            counts.put(plant, user.getInventory().getSeedPacketCount(plant));
        }

        return counts;
    }

    private String grantedPlant(Map<String, Integer> before) {
        for (String plant : user.getCollection().getUnlockedPlants()) {
            int previous = before.getOrDefault(plant, 0);

            if (user.getInventory().getSeedPacketCount(plant) > previous) {
                return plant;
            }
        }

        return null;
    }

    private void chooseSeedPlant(ShopItem item) {
        Array<String> options = new Array<>();
        for (String plant : user.getCollection().getUnlockedPlants()) {
            options.add(plant);
        }

        if (options.size == 0) {
            Toast.error(stage, "You have no unlocked plants to choose from.");
            return;
        }

        SelectBox<String> box = new SelectBox<>(Ui.skin());
        box.setItems(options);

        PamActor art = new PamActor(120f);
        art.setAnimation(GameAssets.get().plantPath(box.getSelected()), "idle");
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                art.setAnimation(GameAssets.get().plantPath(box.getSelected()), "idle");
            }
        });

        Table body = new Table();
        body.add(art).size(120f).padBottom(12f).row();
        body.add(box).width(400f).height(50f).row();
        body.add(Ui.label(item.getBuyUnit() + " packets for "
            + item.getPriceGems() + " gems", "secondary")).padTop(10f).row();

        Modal modal = new Modal("Choose a plant");
        modal.content(body, 440f);
        modal.action("Cancel", "brown", modal::close);
        modal.action("Buy", "green", () -> {
            String plant = box.getSelected();
            modal.close();
            confirm("Buy " + item.getBuyUnit() + " " + plant + " seed packets for "
                + item.getPriceGems() + " gems?", () ->
                report(shop.buyItem(item.getId(), 1, plant, user.getWallet(),
                    user.getGreenHouse(), user.getCollection(), user.getInventory())));
        });
        modal.show(stage);
    }

    private void confirm(String question, Runnable onConfirm) {
        Modal modal = new Modal("Purchase Confirmation");
        modal.message(question);
        modal.action("Cancel", "brown", modal::close);
        modal.action("Confirm", "green", () -> {
            modal.close();
            onConfirm.run();
        });
        modal.show(stage);
    }

    private void report(ShopResult result) {
        switch (result) {
            case SUCCESS:
                UserManager.getInstance().saveAll();
                Toast.info(stage, "Purchase complete.");
                rebuild();
                break;
            case INSUFFICIENT_FUNDS:
                Toast.error(stage, "You do not have enough currency.");
                break;
            case CAPACITY_FULL:
                Toast.error(stage, "You have reached the limit for this item.");
                break;
            case ALREADY_PURCHASED_TODAY:
                Toast.error(stage, "You already bought today's offer.");
                break;
            case PLANT_TYPE_REQUIRED:
                Toast.error(stage, "Choose a plant for this item first.");
                break;
            case PLANT_NOT_UNLOCKED:
                Toast.error(stage, "You have not unlocked that plant.");
                break;
            default:
                Toast.error(stage, "That item is not available.");
                break;
        }
    }
}
