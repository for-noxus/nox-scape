package nox.scripts.noxscape.tasks.mining;

import nox.scripts.noxscape.core.interfaces.IInteractable;
import nox.scripts.noxscape.core.interfaces.ISkillable;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;

public enum MiningEntity implements ISkillable, IInteractable {
    CLAY("Clay", new short[]{6705}, 1, "Clay"),
    COPPER("Copper", new short[]{4645, 4510}, 1, "Copper ore"),
    TIN("Tin", new short[]{53}, 1, "Tin ore"),
    IRON("Iron", new short[]{2576}, 15, "Iron ore"),
    SILVER("Silver", new short[]{74}, 20, "Silver ore"),
    COAL("Coal", new short[]{10508}, 30, "Coal"),
    GOLD("Gold", new short[]{8885}, 40, "Gold ore"),
    MITHRIL("Mithril", new short[]{-22239}, 55, "Mithril ore"),
    ADAMANTITE("Adamantite", new short[]{21662}, 70, "Adamantite ore"),
    RUNITE("Runite", new short[]{-31437}, 85, "Runite ore");

    private String name;
    private short[] modifiedColors;
    private int requiredLevel;
    private String producesItem;

    MiningEntity(String name, short[] modifiedColors, int requiredLevel, String producesItem) {
        this.name = name;
        this.modifiedColors = modifiedColors;
        this.requiredLevel = requiredLevel;
        this.producesItem = producesItem;
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

    @Override
    public String producesItemName() {
        return producesItem;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public String toString() {
        return "MiningEntity{" +
                "name='" + name + '\'' +
                ", modifiedColors=" + Arrays.toString(modifiedColors) +
                ", requiredLevel=" + requiredLevel +
                ", producesItem='" + producesItem + '\'' +
                '}';
    }
}
