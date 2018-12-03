package nox.scripts.noxscape.tasks.mining;

import nox.scripts.noxscape.core.interfaces.INameable;
import nox.scripts.noxscape.core.interfaces.ISkillable;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;

public enum MiningEntities implements ISkillable {
    CLAY("Clay", new short[]{6705}, 1),
    COPPER("Copper", new short[]{4645, 4510}, 1),
    TIN("Tin", new short[]{53}, 1),
    IRON("Iron", new short[]{2576}, 15),
    SILVER("Silver", new short[]{74}, 20),
    COAL("Coal", new short[]{10508}, 30),
    GOLD("Gold", new short[]{8885}, 40),
    MITHRIL("Mithril", new short[]{-22239}, 55),
    ADAMANTITE("Adamantite", new short[]{21662}, 70),
    RUNITE("Runite", new short[]{-31437}, 85);

    private String name;
    private short[] modifiedColors;
    private int requiredLevel;

    MiningEntities(String name, short[] modifiedColors, int requiredLevel) {
        this.name = name;
        this.modifiedColors = modifiedColors;
        this.requiredLevel = requiredLevel;
    }

    /*
        Thanks Explv
     */
    public boolean hasOre(final Entity rockEntity) {
        if (rockEntity.getDefinition() == null) {
            return false;
        }

        short[] colours = rockEntity.getDefinition().getModifiedModelColors();

        if (colours == null) {
            return false;
        }

        for (short rockColour : this.modifiedColors) {
            for (short entityColour : colours) {
                if (rockColour == entityColour) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Skill getSkill() {
        return Skill.MINING;
    }

    @Override
    public int getRequiredLevel() {
        return this.requiredLevel;
    }

    @Override
    public String getInteractAction() {
        return "Mine";
    }
}
