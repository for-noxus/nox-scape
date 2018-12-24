package nox.scripts.noxscape.core.api;

import com.sun.corba.se.spi.activation.ServerAlreadyRegisteredHelper;
import nox.scripts.noxscape.util.Sleep;
import nox.scripts.noxscape.util.WidgetActionFilter;
import nox.scripts.noxscape.util.Wrapper;
import org.osbot.RE;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class QuickExchange extends MethodProvider {

    private int WIDGET_BOX_START = 7;
    private int WIDGET_ROOT_SEARCHITEM = 162;

    private int SPRITE_BUY_ITEM = 1108;
    private int SPRITE_SELL_ITEM = 1106;

    public String WIDGET_TEXT_SEARCHITEM = "Start typing the name of an item to search for it.";

    private String NPC_NAME = "Grand Exchange Clerk";
    private String NPC_ACTION_EXCHANGE = "Exchange";

    private String ITEM_ACTION_OFFER = "Offer";

    private WidgetActionFilter decreaseWidgetFilter = new WidgetActionFilter("-5%");
    private WidgetActionFilter increaseWidgetFilter = new WidgetActionFilter("+5%");

    public QuickExchange(MethodProvider api) {
        exchangeContext(api.getBot());
    }

    public boolean isOpen() {
        return getGrandExchange().isOpen();
    }

    public boolean open() {
        NPC npc = getNpcs().closest(NPC_NAME);

        if (npc == null) {
            log("Unable to locate Grand Exchange NPC");
            return false;
        }

        if (!npc.interact(NPC_ACTION_EXCHANGE)) {
            log("Error interacting with Grand Exchange NPC");
            return false;
        }

        Sleep.until(this::isOpen, 6000, 800);

        return isOpen();
    }

    public boolean hasOpenBox() {
        List<GrandExchange.Box> boxesToCheck;
        // If we're (presumably) a non-mem, we only check the first 3 boxes
        if (getWorlds().isMembersWorld()) {
            boxesToCheck = Arrays.asList(GrandExchange.Box.values());
        } else {
            boxesToCheck = Arrays.asList(GrandExchange.Box.BOX_1, GrandExchange.Box.BOX_2, GrandExchange.Box.BOX_3);
        }

        // Return true if any box is empty
        return boxesToCheck.stream().anyMatch(box -> getGrandExchange().getStatus(box) == GrandExchange.Status.EMPTY);
    }

    public boolean quickBuy(String itemName, int amount, boolean withdrawToBank) throws InterruptedException {
        if (itemName == null)
            return false;

        if (!getInventory().contains("Coins")) {
            log("Inventory contains no money to buy item " + itemName);
            return false;
        }

        GrandExchange.Box boxToUse = tryGetOpenBox();
        if (boxToUse == null) {
            log("No GE Boxes available to use to buy item " + itemName);
            return false;
        }

        if (getGrandExchange().isSellOfferOpen()) {
            if (!getGrandExchange().goBack()) {
                log("Error returning to main GE Screen");
                return false;
            }
        }

        RS2Widget buyWidget = getWidgets().singleFilter(getGrandExchange().getInterfaceId(), widg -> widg.getSecondLevelId() == WIDGET_BOX_START + boxToUse.iIiIIiiiIIiI && widg.getSpriteIndex1() == SPRITE_BUY_ITEM);
        if (buyWidget == null) {
            log("Error locating create-buy-offer widget");
            return false;
        }

        if (!buyWidget.interact()) {
            log("Error interacting with buy-offer widget");
            return false;
        }

        Sleep.until(() -> getGrandExchange().isBuyOfferOpen() && getWidgets().singleFilter(WIDGET_ROOT_SEARCHITEM, w -> w != null && w.getMessage() != null && w.getMessage().equals(WIDGET_TEXT_SEARCHITEM)) != null, 5000, 500);

        getKeyboard().typeString(itemName);

        final Wrapper<RS2Widget> searchItemWidget = new Wrapper<>();
        Sleep.until(() -> {
            searchItemWidget.set(getWidgets().singleFilter(WIDGET_ROOT_SEARCHITEM, f -> f != null && f.getMessage() != null && f.getMessage().equals(itemName)));
            return searchItemWidget.isSet();
        }, 6000, 1000);

        if (!searchItemWidget.isSet()) {
            log(String.format("Unable to locate searched item (%s)", itemName));
            return false;
        }

        if (!searchItemWidget.get().interact()) {
            log(String.format("Error interacting with searched item (%s)", itemName));
            return false;
        }

        Sleep.until(() -> getWidgets().singleFilter(getGrandExchange().getInterfaceId(), w -> w != null && w.getMessage() != null && w.getMessage().equals(itemName)) != null, 10000, 1000);

        if (getGrandExchange().getOfferQuantity() != amount) {
            if (!getGrandExchange().setOfferQuantity(amount)) {
                log(String.format("Error setting offer quantity for item %s and amount %d", itemName, amount));
                return false;
            }
        }

        if (!modifyPricePct(true)) {
            log("Error increasing price");
            return false;
        }

        if (!getGrandExchange().confirm()) {
            log("Error pressing confirm for the purchase of item(s) " + itemName);
            return false;
        }

        Sleep.until(() -> getGrandExchange().getStatus(boxToUse) == GrandExchange.Status.FINISHED_BUY, 10000, 1000);

        if (getGrandExchange().getStatus(boxToUse) == GrandExchange.Status.FINISHED_BUY) {
            if (getInventory().isFull() && !withdrawToBank) {
                log("Inventory was too full to collect bought item(s) " + itemName);
                return false;
            }
            if (getGrandExchange().collect(withdrawToBank)) {
                log(String.format("QuickExchange -- Successfully bought %dx %s at %dGP each.", amount, itemName, getGrandExchange().getItemPrice(boxToUse)));
                return true;
            }
        }

        return true;
    }

    public boolean quickSell(String itemName, int amount) throws InterruptedException {
        if (itemName == null)
            return false;

        if (!getInventory().contains(itemName)) {
            log("Inventory does not contain item indicated to sell");
            return false;
        }

        if (getInventory().getAmount(itemName) < amount) {
            log("Inventory does not contain enough " + itemName + "to sell");
            return false;
        }

        GrandExchange.Box boxToUse = tryGetOpenBox();
        if (boxToUse == null) {
            log("No GE Boxes available to use to sell item " + itemName);
            return false;
        }

        if (getGrandExchange().isBuyOfferOpen()) {
            if (!getGrandExchange().goBack()) {
                log("Error returning to main GE Screen");
                return false;
            }
        }

        if (!getInventory().interact(ITEM_ACTION_OFFER, itemName)) {
            log("Error trying interact with item " + itemName);
            return false;
        }

        Sleep.until(() -> getGrandExchange().isSellOfferOpen(), 2000, 400);

        if (getGrandExchange().getOfferQuantity() != amount) {
            if (!getGrandExchange().setOfferQuantity(amount)) {
                log(String.format("Unable to set offer quantity for item %s and amount %d", itemName, amount));
                return false;
            }
        }

        if (!modifyPricePct(false)) {
            log("Error decreasing price");
            return false;
        }

        if (!getGrandExchange().confirm()) {
            log("Error pressing confirm for the sale of item(s) " + itemName);
            return false;
        }

        Sleep.until(() -> getGrandExchange().getStatus(boxToUse) == GrandExchange.Status.FINISHED_SALE, 10000, 1000);

        if (getGrandExchange().getStatus(boxToUse) == GrandExchange.Status.FINISHED_SALE) {
            if (getGrandExchange().collect(true)) {
                log(String.format("QuickExchange -- Successfully sold %dx %s at %dGP each.", amount, itemName, getGrandExchange().getItemPrice(boxToUse)));
                return true;
            }
        }

        return false;
    }

    private boolean modifyPricePct(boolean increase) throws InterruptedException {
        RS2Widget widg = getWidgets().singleFilter(getGrandExchange().getInterfaceId(), increase ? increaseWidgetFilter : decreaseWidgetFilter);
        String message = increase ? "increase" : "decrease";
        if (widg == null) {
            log("Unable to locate 5% " + message + " filter");
            return false;
        }

        for (int i = 0; i < random(2, 5); i++) {
            if (!widg.interact()) {
                log("Error interacting with 5% " + message + " widget");
                return false;
            }
            sleep(random(20,75));
        }

        return true;
    }

    private GrandExchange.Box tryGetOpenBox() throws InterruptedException {
        if (!isOpen()) {
            log("Grand Exchange is not open");
            return null;
        }

        if (!hasOpenBox()) {
            if (getGrandExchange().isOfferScreenOpen()) {
                if (!getGrandExchange().goBack()) {
                    log("Error returning to main GE Screen");
                    return null;
                }
            }
            if (getGrandExchange().collect(true)) {
                sleep(1000);
                if (!hasOpenBox()) {
                    log("No open GrandExchange Boxes, and unable to collect any boxes");
                    return null;
                }
            }
        }

        return Arrays.stream(GrandExchange.Box.values()).filter(f -> getGrandExchange().getStatus(f) == GrandExchange.Status.EMPTY).findFirst().orElse(null);
    }
}
