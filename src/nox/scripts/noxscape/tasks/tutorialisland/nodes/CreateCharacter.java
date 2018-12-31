package nox.scripts.noxscape.tasks.tutorialisland.nodes;

import nox.scripts.noxscape.core.NoxScapeNode;
import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.util.Sleep;
import nox.scripts.noxscape.util.WidgetActionFilter;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CreateCharacter extends NoxScapeNode {

    private final int WIDGET_ROOT_DISPLAYNAME = 558;
    private final int WIDGET_ROOT_DESIGNER = 269;
    private final int WIDGET_ROOT_CHATBOX = 162;
    private final int WIDGET_ROOT_CHATBOX_INSTRUCTIONS = 263;

    private final String TEXT_DISPLAYNAME_INSTRUCTIONS = "What name would you like to check";
    private final String TEXT_DISPLAYNAME_UNAVAILABLE = "Sorry, this display name";
    private final String TEXT_DISPLAYNAME_REQUESTING = "Requesting";
    private final String TEXT_LOOKUP_DISPLAYTNAME = "Look up name";
    private final String TEXT_LOOKUP_SETNAME = "Set name";
    private final String TEXT_DESIGNER_INSTRUCTIONS = "need to set the appearance of your character";
    private final String TEXT_DESIGNER_ACCEPT = "Accept";

    private final String[] DESIGNER_PHYSICAL_OPTIONS = new String[] { "Change head", "Change jaw", "Change torso", "Change arms", "Change hands", "Change legs", "Change feet" };
    private final String[] DESIGNER_COLOR_OPTIONS = new String[] { "Recolour hair", "Recolour torso", "Recolour legs", "Recolour feet", "Recolour skin" };
    private final String DESIGNER_GENDER_OPTION_FEMALE = "Female";

    public CreateCharacter(NoxScapeNode next, ScriptContext ctx, String message) {
        super(next, ctx, message);
    }

    @Override
    public boolean isValid() {
        return ctx.getWidgets().isVisible(WIDGET_ROOT_DISPLAYNAME) || ctx.getWidgets().isVisible(WIDGET_ROOT_DESIGNER);
    }

    @Override
    public int execute() throws InterruptedException {
        // From the validation we are guaranteed to be in the character creation screen
        // Is our name awaiting validation?
        RS2Widget requestingWidget = ctx.getWidgets().getWidgetContainingText(WIDGET_ROOT_DISPLAYNAME,TEXT_DISPLAYNAME_REQUESTING);
        if (requestingWidget != null) {
            return 1000;
        }

        // Are we in the lookup-name phase?
        RS2Widget lookupNameWidget = ctx.getWidgets().singleFilter(WIDGET_ROOT_DISPLAYNAME, f -> {
            String[] interactActions = f.getInteractActions();
            return f.getType() == 0 && interactActions != null && interactActions.length > 0 && Arrays.stream(interactActions).anyMatch(a -> a.equals(TEXT_LOOKUP_DISPLAYTNAME));
        });
        RS2Widget setNameWidget = ctx.getWidgets().getWidgetContainingText(WIDGET_ROOT_DISPLAYNAME, TEXT_LOOKUP_SETNAME);

        if (lookupNameWidget != null && lookupNameWidget.interact()) {
            ctx.logClass(this, "Setting the character's name..");
            Sleep.until(() -> ctx.getWidgets().getWidgetContainingText(WIDGET_ROOT_CHATBOX, TEXT_DISPLAYNAME_INSTRUCTIONS) != null, 2000, 500);
            String generatedName = generateName();
            ctx.logClass(this, "Attempting to use name (" + generatedName + ")");
            ctx.getKeyboard().typeString(generatedName);

            Sleep.until(() -> {
                setNameWidget.refresh();
                RS2Widget nameWidget = ctx.getWidgets().getWidgetContainingText(WIDGET_ROOT_DISPLAYNAME, generatedName);
                RS2Widget setNameActionWidget = ctx.getWidgets().singleFilter(WIDGET_ROOT_DISPLAYNAME, new WidgetActionFilter(TEXT_LOOKUP_SETNAME));
                return setNameActionWidget != null && nameWidget != null;
            }, 10000, 2000);

            RS2Widget unavailableWidget = ctx.getWidgets().getWidgetContainingText(WIDGET_ROOT_DISPLAYNAME, TEXT_DISPLAYNAME_UNAVAILABLE);
            if (unavailableWidget != null) {
                ctx.logClass(this, String.format("Name (%s) was unavailable. Retrying..", generatedName));
                return new Random().nextInt(5000) + 500;
            } else {
                if (setNameWidget != null) {
                    if (setNameWidget.interact()) {
                        ctx.logClass(this, "Successfully created account (" + generatedName + ").");
                        return new Random().nextInt(5000) + 1000;
                    }
                }
            }
        } else { //Otherwise, we must be in the designer phase
            RS2Widget appearanceWidget = ctx.getWidgets().getWidgetContainingText(WIDGET_ROOT_CHATBOX_INSTRUCTIONS, TEXT_DESIGNER_INSTRUCTIONS);
            if (appearanceWidget == null) {
                ctx.logClass(this, "Couldn't find an entrypoint to name or design character..");
                ctx.getMouse().moveOutsideScreen();
                return new Random().nextInt(5000) + 1000;
            }
            ctx.logClass(this, "Setting the character's appearance..");
            boolean isFemale = new Random().nextBoolean();
            if (isFemale) {
                RS2Widget femaleWidget = ctx.getWidgets().getWidgetContainingText(WIDGET_ROOT_DESIGNER, DESIGNER_GENDER_OPTION_FEMALE);
                if (femaleWidget != null && femaleWidget.interact()) {
                    ctx.logClass(this, "Successfully made the account a female.");
                }
            }

            try {
                randomlyChooseCharacterOptions(DESIGNER_PHYSICAL_OPTIONS);
                randomlyChooseCharacterOptions(DESIGNER_COLOR_OPTIONS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            RS2Widget acceptWidget = ctx.getWidgets().getWidgetContainingText(WIDGET_ROOT_DESIGNER, TEXT_DESIGNER_ACCEPT);
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
    private String[] animals = new String[] { "Cat", "Bird", "Dog", "Bear", "Lion", "Wolf", "Horse", "Dear", "Turtle", "Fox", "Husky", "Rabbit", "Tiger", "Squirrel", "Bat", "Monkey", "Dalmation", "Calico", "Whale", "Rhino", "Goat", "Pig", "Cow", "Bull", "Shark", "Human", "Man", "woman" };

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
            List<RS2Widget> widgets = ctx.getWidgets().filter(WIDGET_ROOT_DESIGNER, f -> f.getToolTip() != null && f.getToolTip().equals(opt));
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
