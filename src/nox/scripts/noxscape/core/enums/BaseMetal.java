package nox.scripts.noxscape.core.enums;

import nox.scripts.noxscape.core.interfaces.INameable;

public enum BaseMetal implements INameable {
    BRONZE("Bronze", 0),
    IRON("Iron", 1),
    BLACK("Black", 2),
    STEEL("Steel", 3),
    MITHRIL("Mithril", 4),
    ADAMANTITE("Adamantite", 5),
    RUNITE("Rune", 6);

    private String name;
    private int position;

    BaseMetal(String name, int position) {
        this.name = name;
        this.position = position;
    }

    @Override
    public String getName() {
        return name;
    }
}
