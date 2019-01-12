package nox.scripts.noxscape.core.api;

import nox.scripts.noxscape.core.interfaces.ICombatable;
import nox.scripts.noxscape.tasks.base.combat.CombatPreferenceProfile;
import nox.scripts.noxscape.tasks.base.combat.Food;
import nox.scripts.noxscape.util.NRandom;
import nox.scripts.noxscape.util.Sleep;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;

public class CombatHelper extends MethodProvider {

        private final int ANIM_EATING = 829;

        private final CombatPreferenceProfile profile;

        public CombatHelper(MethodProvider api, CombatPreferenceProfile profile) {
            this.profile = profile;
            exchangeContext(api.getBot());
        }

        public boolean isInCombatArea() {
            return profile.getAreaToFight() == null ?
                    getNpcs().closest(f -> profile.getNpcsToFight().stream().anyMatch(a -> a.getName().equals(f.getName()) && a.getLevel() == f.getLevel())) != null :
                    profile.getAreaToFight().contains(myPosition());
        }

        public NPC getNextTarget() {
            List<ICombatable> toFight = profile.getNpcsToFight();
            if (toFight == null || toFight.size() == 0)
                return null;

            List<NPC> npcsToFight = getNpcs().filter(f ->
                            f.getName() != null && f.getLevel() > 0 && f.getPosition() != null &&
                            (profile.getAreaToFight() == null || profile.getAreaToFight().contains(f.getPosition())) &&
                            f.isAttackable() &&
                            toFight.stream().anyMatch(a -> a.getLevel() == f.getLevel() && a.getName().equals(f.getName())));

            if (npcsToFight == null || npcsToFight.size() == 0)
                return null;

            NPC closestNPC = npcsToFight.stream().min(Comparator.comparingInt(npc -> myPosition().distance(npc))).orElse(null);

            int distanceFromClosest = closestNPC.getPosition().distance(myPosition());

            if (distanceFromClosest <= 3) {
                return closestNPC;
            }

            return npcsToFight.stream()
                    .filter(f -> f.getPosition().distance(myPosition()) - distanceFromClosest <= 2)
                    .sorted((n1, n2) -> NRandom.exact(-1, 2))
                    .findAny()
                    .orElse(null);
        }

        public boolean hasFood() {
            if (profile.getFoods() == null || profile.getFoods().size() == 0)
                return true;

            String[] foods = profile.getFoods().stream().map(Food::getName).toArray(String[]::new);

            return getInventory().contains(foods);
        }

        public boolean checkHealth() {
            if (profile != null) {
                if (shouldEat()) {
                    if (profile.getFoods() == null || profile.getFoods().size() == 0) {
                        log("Script needs to eat, but no foods were provided!");
                        return false;
                    }

                    String[] foods = profile.getFoods().stream().map(Food::getName).toArray(String[]::new);
                    while (shouldEat()) {
                        int emptySlots = getInventory().getEmptySlotCount();

                        if (!getInventory().contains(foods)) {
                            log("Inventory did not contain any food to eat " + Arrays.toString(foods));
                            return false;
                        }

                        if (!getInventory().interact(null, foods)) {
                            log("Unable to eat any foods " + Arrays.toString(foods));
                            return false;
                        }

                        Sleep.until(() -> getInventory().getEmptySlotCount() != emptySlots && myPlayer().getAnimation() != ANIM_EATING, 4000, 500);
                    }
                }
            }

            return true;
        }

        private boolean shouldEat() {
            return profile.shouldEat(getSkills().getDynamic(Skill.HITPOINTS), getSkills().getStatic(Skill.HITPOINTS));
        }
}
