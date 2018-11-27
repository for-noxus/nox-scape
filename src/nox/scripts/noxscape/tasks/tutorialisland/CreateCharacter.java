package nox.scripts.noxscape.tasks.tutorialisland;

import com.sun.scenario.effect.impl.prism.PrEffectHelper;
import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.Tracker;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CreateCharacter extends NoxScapeNode {

    public CreateCharacter(NoxScapeNode next, ScriptContext ctx, String message, Tracker tracker) {
        super(next, ctx, message, tracker);
    }

    @Override
    public boolean isValid() {
        return ctx.getWidgets().isVisible(TutorialIslandConstants.WIDGET_ROOT_DISPLAYNAME) || ctx.getWidgets().isVisible(TutorialIslandConstants.WIDGET_ROOT_DESIGNER);
    }

    @Override
    public int execute() {
        // From the validation we are guaranteed to be in the character creation screen
        // Is our name awaiting validation?
        RS2Widget requestingWidget = ctx.getWidgets().getWidgetContainingText(TutorialIslandConstants.WIDGET_ROOT_DISPLAYNAME,TutorialIslandConstants.TEXT_DISPLAYNAME_REQUESTING);
        if (requestingWidget != null) {
            return 1000;
        }

        // Are we in the lookup-name phase?
        RS2Widget lookupNameWidget = ctx.getWidgets().getWidgetContainingText(TutorialIslandConstants.WIDGET_ROOT_DISPLAYNAME, TutorialIslandConstants.TEXT_LOOKUP_DISPLAYTNAME);
        if (lookupNameWidget != null && lookupNameWidget.interact()) {
            ctx.logClass(this, "Setting the character's name..");
            Sleep.sleepUntil(() -> ctx.getWidgets().getWidgetContainingText(TutorialIslandConstants.WIDGET_CHATBOX, TutorialIslandConstants.TEXT_DISPLAYNAME_INSTRUCTIONS) != null, 2000, 500);
            String generatedName = generateName();
            ctx.logClass(this, "Attempting to use name (" + generatedName + ")");
            ctx.getKeyboard().typeString(generatedName);

            Sleep.sleepUntil(() -> {
                lookupNameWidget.refresh();
                RS2Widget unavailableWidget = ctx.getWidgets().getWidgetContainingText(TutorialIslandConstants.WIDGET_ROOT_DISPLAYNAME,TutorialIslandConstants.TEXT_DISPLAYNAME_UNAVAILABLE);
                return (lookupNameWidget != null && lookupNameWidget.getSpriteIndex1() == TutorialIslandConstants.WIDGET_SPRITE_LOOKUPNAME_OPEN) || unavailableWidget != null && unavailableWidget.isVisible();
            }, 4000, 600);

            RS2Widget unavailableWidget = ctx.getWidgets().getWidgetContainingText(TutorialIslandConstants.WIDGET_ROOT_DISPLAYNAME, TutorialIslandConstants.TEXT_DISPLAYNAME_UNAVAILABLE);
            if (unavailableWidget != null) {
                ctx.logClass(this, String.format("Name (%s) was unavailable. Retrying..", generatedName));
                return new Random().nextInt(5000) + 500;
            } else {
                RS2Widget setNameWidget = ctx.getWidgets().getWidgetContainingText(TutorialIslandConstants.WIDGET_ROOT_DISPLAYNAME,TutorialIslandConstants.TEXT_LOOKUP_SETNAME);
                if (setNameWidget != null) {
                    if (setNameWidget.interact()) {
                        ctx.logClass(this, "Successfully created account (" + generatedName + ").");
                        return new Random().nextInt(5000) + 1000;
                    }
                }
            }
        } else { //Otherwise, we must be in the designer phase
            RS2Widget appearanceWidget = ctx.getWidgets().getWidgetContainingText(TutorialIslandConstants.WIDGET_CHATBOX_DESIGNER_INSTRUCTIONS, TutorialIslandConstants.TEXT_DESIGNER_INSTRUCTIONS);
            if (appearanceWidget == null) {
                ctx.logClass(this, "Couldn't find an entrypoint to name or design character..");
                ctx.getMouse().moveOutsideScreen();
                return new Random().nextInt(5000) + 1000;
            }
            ctx.logClass(this, "Setting the character's appearance..");
            boolean isFemale = new Random().nextBoolean();
            if (isFemale) {
                RS2Widget femaleWidget = ctx.getWidgets().getWidgetContainingText(TutorialIslandConstants.WIDGET_ROOT_DESIGNER, TutorialIslandConstants.DESIGNER_GENDER_OPTION_FEMALE);
                if (femaleWidget != null && femaleWidget.interact()) {
                    ctx.logClass(this, "Successfully made the account a female.");
                }
            }

            try {
                randomlyChooseCharacterOptions(TutorialIslandConstants.DESIGNER_PHYSICAL_OPTIONS);
                randomlyChooseCharacterOptions(TutorialIslandConstants.DESIGNER_COLOR_OPTIONS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            RS2Widget acceptWidget = ctx.getWidgets().getWidgetContainingText(TutorialIslandConstants.WIDGET_ROOT_DESIGNER, TutorialIslandConstants.TEXT_DESIGNER_ACCEPT);
            if (acceptWidget != null && acceptWidget.interact()) {
                ctx.logClass(this, "Successfully designed character!");
            } else {
                ctx.logClass(this, "Having trouble clicking Accept at Character Designer..");
                return 600;
            }
        }
        return 2000;
    }

    private String[] adjectives = new String[] { "Homely", "Quant", "Hungry", "Real", "Hurt", "Alert", "Alive", "Ill", "Rich", "Amused", "Angry", "Scary", "Dizzy", "Shiny", "Drab", "Shy", "Dull", "Itchy", "Silly", "Sleepy", "Eager", "Easy", "Smoggy", "Awful", "Elated", "Jolly", "Sore", "Joyous", "Bad", "Kind", "Better", "Stormy", "Lazy", "Black", "Light", "Stupid", "Bloody", "Lively", "Blue", "Evil", "Lonely", "Super", "Long", "Lovely", "Bored", "Lucky", "Tame", "Brainy", "Tender", "Brave", "Fair", "Tense", "Misty", "Bright", "Famous", "Modern", "Tasty", "Busy", "Fancy", "Muddy", "Calm", "Fierce", "Mushy", "Filthy", "Tired", "Fine", "Tough", "Nasty", "Clean", "Frail", "Clear", "Nice", "Ugly", "Clever", "Nutty", "Cloudy", "Clumsy", "Funny", "Upset", "Gentle", "Odd", "Gifted", "Open", "Vast", "Good", "Crazy", "Weary", "Creepy", "Plain", "Wicked", "Cruel", "Grumpy", "Poised", "Wild", "Poor", "Witty", "Cute", "Happy", "Wrong", "Dark", "Proud", "Dead", "Zany" };
    private String[] animals = new String[] { "cat", "bird", "dog", "bear", "lion", "wolf", "horse", "dear", "turtle", "fox", "husky", "rabbit", "tiger", "squirrel", "bat", "monkey", "dalmation", "calico", "whale", "rhino", "goat", "pig", "cow", "bull", "shark", "human", "man", "woman" };

    private String generateName() {
        Random r = new Random();
        String generated = "";
        while (generated.isEmpty() || generated.length() >= 13) {
            generated = String.format("%s%s", adjectives[r.nextInt(adjectives.length)], animals[r.nextInt(animals.length)]);
            int numsToPad = Math.min(r.nextInt(4), 12 - generated.length());
            generated = String.format("%s%d", generated, Math.round(r.nextDouble() * Math.pow(10.0, numsToPad)));
        }
        return generated;
    }

    private void randomlyChooseCharacterOptions(String... options) throws InterruptedException {
        Random r = new Random();
        // Whether our user prefers to use the front or back arrows to navigate...most people use front
        int preferredStartingWidget = r.nextInt(5) == 0 ? 0 : 1;
        for (String opt: options) {
            List<RS2Widget> widgets = ctx.getWidgets().filter(TutorialIslandConstants.WIDGET_ROOT_DESIGNER, f -> f.getToolTip() != null && f.getToolTip().equals(opt));
            if (widgets == null || widgets.size() != 2) {
                ctx.logClass(this, String.format("Couldn't find option (%s) in character designer!", opt));
                continue;
            }
            RS2Widget favoredWidget = widgets.remove(preferredStartingWidget);
            RS2Widget oddWidget = widgets.remove(0);
            int numClicks = r.nextInt(8);
            if (numClicks == 0) {
                favoredWidget.hover();
                MethodProvider.sleep(r.nextInt(250) + 50);
            } else {
                for (int i = 0; i < numClicks; i++) {
                    favoredWidget.interact();
                    MethodProvider.sleep(r.nextInt(2000) + 80);
                    if (r.nextInt(5) == 0) {
                        for (int j = 0; j < r.nextInt(3); j++) {
                            oddWidget.interact();
                            MethodProvider.sleep(r.nextInt(2000) + 80);
                        }
                    }
                }
            }
            MethodProvider.sleep(r.nextInt(2000));
        }
    }
}
