package nox.scripts.noxscape.tasks.mining;

import nox.scripts.noxscape.core.interfaces.IBankable;
import nox.scripts.noxscape.core.interfaces.ILocateable;
import nox.scripts.noxscape.core.interfaces.INameable;
import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import org.osbot.rs07.api.DepositBox;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;

import java.util.Arrays;

public enum MiningLocation implements INameable, ILocateable, IBankable {

    RIMMINGTON_CLAY("Clay at Rimmington", BankLocation.DRAYNOR, BankLocation.PORTSARIM, MiningEntity.CLAY, new Position(2986, 3240, 0), new Position(2987, 3239, 0)),
    RIMMINGTON_COPPER("Copper at Rimmington", BankLocation.DRAYNOR, BankLocation.PORTSARIM, MiningEntity.COPPER, new Position(2978, 3247, 0), new Position(2977, 3248, 0), new Position(2977, 3246, 0)),
    RIMMINGTON_TIN("Tin at Rimmington", BankLocation.DRAYNOR, BankLocation.PORTSARIM, MiningEntity.TIN, new Position(2984, 3236, 0), new Position(2986, 3236, 0)),
    RIMMINGTON_IRON("Iron at Rimmington", BankLocation.DRAYNOR, BankLocation.PORTSARIM, MiningEntity.IRON, new Position(2969, 3241, 0), new Position(2969, 3239, 0), new Position(2981, 3234, 0), new Position(2982, 3233, 0)),
    RIMMINGTON_GOLD("Gold at Rimmington", BankLocation.DRAYNOR, BankLocation.PORTSARIM, MiningEntity.GOLD, new Position(2976, 3234, 0)),

    LUMBY_SE_TIN("Tin south of Lumbridge", BankLocation.LUMBRIDGE_UPPER, MiningEntity.TIN, new Position(3223, 3147, 0)),
    LUMBY_SE_COPPER("Copper south of Lumbridge", BankLocation.LUMBRIDGE_UPPER, MiningEntity.TIN, new Position(3229, 3147, 0), new Position(3230, 3146, 0), new Position(3228, 3145, 0), new Position(3230, 3148, 0)),

    LUMBY_SW_COAL("Coal southwest of Lumbridge", BankLocation.LUMBRIDGE_UPPER, MiningEntity.COAL, new Position(3145, 3152, 0), new Position(3145, 3150, 0)),
    LUMBY_SW_MITH("Mithril southwest of Lumbridge", BankLocation.LUMBRIDGE_UPPER, MiningEntity.MITHRIL, new Position(3144, 3136, 0)),
    LUMBY_SW_ADDY("Adamantite southwest of Lumbridge", BankLocation.LUMBRIDGE_UPPER, MiningEntity.ADAMANTITE, new Position(3147, 3147, 0)),

    VAAROK_E_TIN("Tin southeast of Varrock", BankLocation.VARROCK_EAST, MiningEntity.TIN, new Position(3282, 3363, 0)),
    VAAROK_E_COPPER("Copper southeast of Varrock", BankLocation.VARROCK_EAST, MiningEntity.COPPER, new Position(3287, 3364, 0), new Position(3286, 3361, 0), new Position(3289, 3362, 0), new Position(3286, 3365, 0)),
    VAAROK_E_IRON("Iron southeast of Varrock", BankLocation.VARROCK_EAST, MiningEntity.IRON, new Position(3286, 3368, 0)),

    VAAROK_W_CLAY("Clay southwest of Varrock", BankLocation.VARROCK_WEST, MiningEntity.CLAY, new Position(3180, 3371, 0), new Position(3183, 3378, 0), new Position(3182, 3377, 0), new Position(3182, 3377, 0)),
    VAAROK_W_TIN("Tin southwest of Varrock", BankLocation.VARROCK_WEST, MiningEntity.TIN, new Position(3177, 3365, 0), new Position(3177, 3369, 0), new Position(3182, 3376, 0)),
    VAAROK_W_IRON("Iron southwest of Varrock", BankLocation.VARROCK_WEST, MiningEntity.IRON, new Position(3175, 3367, 0)),
    VAAROK_W_SILVER("Silver southwest of Varrock", BankLocation.VARROCK_WEST, MiningEntity.SILVER, new Position(3177, 3365, 0), new Position(3176, 3366, 0));

    public final String name;
    public final BankLocation closestBank;
    public final BankLocation closestDepositBox;
    public final Position[] positions;
    public final MiningEntity rock;

    MiningLocation(String name, BankLocation closestBank, MiningEntity rock, Position... positions) {
        this(name, closestBank, null, rock, positions);
    }

    MiningLocation(String name, BankLocation closestBank, BankLocation closestDepositBox, MiningEntity rock, Position... positions) {
        this.name = name;
        this.closestBank = closestBank;
        this.closestDepositBox = closestDepositBox;
        this.positions = positions;
        this.rock = rock;
    }

    public boolean shouldStayInTile() {
        return Arrays.asList(MiningEntity.IRON, MiningEntity.CLAY, MiningEntity.TIN, MiningEntity.COPPER).contains(rock);
    }

    @Override
    public Position getPosition() {
        return positions[0];
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BankLocation getBank() {
        return closestBank;
    }

    @Override
    public BankLocation getDepositBox() {
        return closestDepositBox;
    }
}
