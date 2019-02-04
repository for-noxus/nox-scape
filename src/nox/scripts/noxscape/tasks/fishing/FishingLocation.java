package nox.scripts.noxscape.tasks.fishing;

import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.interfaces.IBankable;
import nox.scripts.noxscape.core.interfaces.ILocateable;
import nox.scripts.noxscape.core.interfaces.INameable;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import org.osbot.rs07.api.map.Position;

import java.util.function.Predicate;

public enum FishingLocation implements INameable, ILocateable, IBankable {
    NET_MUSA_POINT("Small net at Musa Point", FishingTool.NET, null, new Position(2995, 3165, 0), BankLocation.PORTSARIM),
    NET_DRAYNOR("Small net at Draynor", FishingTool.NET, null, new Position(3086, 3230, 0), BankLocation.DRAYNOR);

    private final String friendlyName;
    private final FishingTool fishingTool;
    private final FishingTool tertiaryEntity;
    private final Position position;
    private final BankLocation closestBank;
    private final Predicate<ScriptContext> condition;

    FishingLocation(String friendlyName, FishingTool fishingTool, FishingTool tertiaryEntity, Position position, BankLocation closestBank) {
        this(friendlyName, fishingTool, tertiaryEntity, position, closestBank, ctx -> true);
    }

    FishingLocation(String friendlyName, FishingTool fishingTool, FishingTool tertiaryEntity, Position position, BankLocation closestBank, Predicate<ScriptContext> condition) {

        this.friendlyName = friendlyName;
        this.fishingTool = fishingTool;
        this.tertiaryEntity = tertiaryEntity;
        this.position = position;
        this.closestBank = closestBank;
        this.condition = condition;
    }

    public FishingTool getFishingTool() {
        return fishingTool;
    }

    public FishingTool getTertiaryEntity() {
        return tertiaryEntity;
    }

    public boolean meetsCondition(ScriptContext ctx) {
        return condition.test(ctx);
    }

    @Override
    public BankLocation getBank() {
        return closestBank;
    }

    @Override
    public BankLocation getDepositBox() {
        return closestBank;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public String getName() {
        return friendlyName;
    }
}
