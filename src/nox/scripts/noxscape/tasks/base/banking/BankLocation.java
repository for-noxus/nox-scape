package nox.scripts.noxscape.tasks.base.banking;

import nox.scripts.noxscape.core.ScriptContext;
import nox.scripts.noxscape.core.interfaces.ILocateable;
import nox.scripts.noxscape.core.interfaces.INameable;
import nox.scripts.noxscape.util.LocationUtils;
import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.script.MethodProvider;

import java.util.Arrays;
import java.util.Comparator;

public enum BankLocation implements INameable, ILocateable {
    AL_KHARID("Al Kharid", Banks.AL_KHARID, false),
    ARCEUUS_HOUSE("Arceuus House", Banks.ARCEUUS_HOUSE, true),
    ARDOUGNE_NORTH("Ardougne North", Banks.ARDOUGNE_NORTH, false),
    ARDOUGNE_SOUTH("Ardougne South", Banks.ARDOUGNE_SOUTH, false),
    CAMELOT("Camelot", Banks.CAMELOT, false),
    CANIFIS("Canifis", Banks.CANIFIS, false),
    CASTLE_WARS("Castle Wars", Banks.CASTLE_WARS, false),
    CATHERBY("Catherby", Banks.CATHERBY, false),
    DRAYNOR("Draynor", Banks.DRAYNOR, true),
    DUEL_ARENA("Duel Arena", Banks.DUEL_ARENA, true),
    EDGEVILLE("Edgeville", Banks.EDGEVILLE, true),
    FALADOR_EAST("Falador East", Banks.FALADOR_EAST, true),
    FALADOR_WEST("Falador West", Banks.FALADOR_WEST, true),
    GNOME_STRONGHOLD("Gnome Stronghold", Banks.GNOME_STRONGHOLD, false),
    GRAND_EXCHANGE("Grand Exchange", Banks.GRAND_EXCHANGE, true),
    HOSIDIUS_HOUSE("Hosidius House", Banks.HOSIDIUS_HOUSE, false),
    LOVAKENGJ_HOUSE("Lovakengj House", Banks.LOVAKENGJ_HOUSE, false),
    LOVAKITE_MINE("Lovakite Mine", Banks.LOVAKITE_MINE, false),
    LUMBRIDGE_LOWER("Lumbridge Lower", Banks.LUMBRIDGE_LOWER, false),
    LUMBRIDGE_UPPER("Lumbridge Upper", Banks.LUMBRIDGE_UPPER, true),
    PEST_CONTROL("Pest Control", Banks.PEST_CONTROL, false),
    PISCARILIUS_HOUSE("Piscarilius House", Banks.PISCARILIUS_HOUSE, false),
    SHAYZIEN_HOUSE("Shayzien House", Banks.SHAYZIEN_HOUSE, false),
    TZHAAR("Tzhaar", Banks.TZHAAR, false),
    VARROCK_EAST("Varrock East", Banks.VARROCK_EAST, true),
    VARROCK_WEST("Varrock West", Banks.VARROCK_WEST, true),
    YANILLE("Yanille", Banks.YANILLE, false),
    BARBARIANASSAULTBANK("Barbarian Assault Bank", new Area(2534, 3576, 2537, 3572), false), //Barbarian Assault Bank - Members
    BURGHDEROTTBANK("Burgh de Rott Bank", new Area(3496, 3213, 3499, 3210), false), //Burgh de Rott Bank - Members, started In Aid of the Myreque
    CRAFTINGGUILDBANK("Crafting Guild Bank", new Area(2933, 3284, 2936, 3281), true), //Crafting Guild Bank - F2P, crafting level 40 + brown apron
    ETCETERIABANK("Etceteria Bank", new Area(2618, 3896, 2620, 3893), false), //Etceteria Bank - Members, Fremmy Trials finished
    FISHINGTRAWLERBANK("Fishing Trawler Bank", new Area(2661, 3162, 2665, 3160), false), //Fishing Trawler Bank - Members
    FISHINGGUILDBANK("Fishing Guild Bank", new Area(2584, 3422, 2588, 3418), false), //Fishing Guild Bank - Members, 63 fishing (with boost) or 68
    GRANDTREEWEST("Grand Tree West", new Area(2440, 3489, 2442, 3487).setPlane(1), false), //Grand Tree West - Members, requires one time door minigame? or tele via grand seed pod
    GRANDTREESOUTH("Grand Tree South", new Area(2448, 3482, 2450, 3479).setPlane(1), false), //Grand Tree South - Members, requires one time door minigame? or tele via grand seed pod
    JATISZOBANK("Jatiszo Bank", new Area(2415, 3803, 2418, 3801), false), //Jatiszo Bank - Members, Fremmy Trials finished, fremmy isle started
    KINGDOMOFGREATKOURENDBANK("Kingdom of Great Kourend Bank", new Area(1610, 3683, 1613, 3680).setPlane(2), false), //Kingdom of Great Kourend Bank - Members
    LLETYABANK("Lletya Bank", new Area(2350, 3163, 2354, 3162), false), //Lletya Bank - Members, started Mourning's Ends Part I
    LUNARISLEBANK("Lunar Isle Bank", new Area(2097, 3919, 2102, 3917), false), //Lunar Isle Bank - Members, about half way into Lunar Diplomacy
    LANDSENDBANK("Lands End Bank", new Area(1508, 3423, 1511, 3419), false), //Lands End Bank - Members
    NARDAHBANK("Nardah Bank", new Area(3424, 2892, 3430, 2889), false), //Nardah Bank - Members, requires getting into desert
    NEITIZNOTBANK("Neitiznot Bank", new Area(2335, 3808, 2337, 3805), false), //Neitiznot Bank - Members, Fremmy trials finished
    PORTPHASMATYSBANK("Port Phasmatys Bank", new Area(3686, 3471, 3691, 3463), false), //Port Phasmatys Bank - Members, requires Priest in Peril (can charter or use ecoto tokens to enter)
    PORTSARIM("Port Sarim Deposit", new Area(3043, 3235, 3046, 3236), true, true),
    PISCATORISBANK("Piscatoris Bank", new Area(2327, 3690, 2332, 3687), false), //Piscatoris Bank - Members, Swan Song finished
    SHILOVILLAGEBANK("Shilo Village Bank", new Area(2849, 2955, 2855, 2953), false), //Shilo Village Bank - Members, Shilo Villlage finished
    SANDCRABSBANK("Sandcrabs Bank", new Area(1717, 3466, 1722, 3463), false), //Sandcrabs Bank - Members, located in Zeah
    SHANTAYPASSBANK("Shantay Pass Bank", new Area(3305, 3123, 3308, 3119), false), //Shantay Pass Bank - F2P
    SULPHURMINE("Sulphur Mine", new Area(1453, 3859, 1458, 3856), false), //Sulphur Mine - Members, located in Zeah
    TUTORIALISLANDBANK("Tutorial Island Bank", new Area(3120, 3124, 3123, 3120), true), //Tutorial Island Bank - F2P
    TREEGNOMESTRONGHOLDBANK("Tree Gnome Stronghold Bank", new Area(2444, 3427, 2446, 3422).setPlane(1), false), //Tree Gnome Stronghold Bank - Members, requires one time door minigame? or tele via grand seed pod
    VINERYBANK("Vinery Bank", new Area(1802, 3571, 1808, 3571), false), //Vinery Bank - Members, located in Zeah, requires no favour to use
    WARRIORSGUILDBANK("Warriors Guild Bank", new Area(2843, 3544, 2846, 3539), false), //Warriors Guild Bank - Members, Attack + Strength >= 130
    WOODCUTTINGGUILDBANK("Woodcutting Guild Bank", new Area(1589, 3480, 1593, 3476), false), //Woodcutting Guild Bank - Members, requires 60 wc + 75% hosidius house favour
    ZEAHCOOKINGBANK("Zeah Cooking Bank", new Area(1653, 3613, 1658, 3607), false); //Zeah Cooking Bank - Members, located on Zeah


    private final String name;
    private final Area bankArea;
    private final boolean isF2P;
    private final boolean isDepositBox;

    BankLocation(String name, Area bankArea, boolean isF2P) {
        this(name, bankArea, isF2P, false);
    }

    BankLocation(String name, Area bankArea, boolean isF2P, boolean isDepositBox) {
        this.name = name;
        this.bankArea = bankArea;
        this.isF2P = isF2P;
        this.isDepositBox = isDepositBox;
    }

    public static BankLocation closestToMeOrDestination(ScriptContext ctx, Position destinationPosition) {
        BankLocation closestToDestination = closestTo(ctx, destinationPosition, false);
        BankLocation closestToPlayer = closestTo(ctx, ctx.myPosition(), false);

        return closestToDestination.getPosition().distance(ctx.myPosition()) > closestToPlayer.getPosition().distance(ctx.myPosition()) ?
                closestToPlayer :
                closestToDestination;
    }

    public static BankLocation closestTo(MethodProvider ctx, Position pos, boolean isDepositBoxAcceptable) {
        Comparator<BankLocation> euclideanDistance = Comparator.comparingInt(a -> a.bankArea.getRandomPosition().distance(pos));
        boolean isMember = ctx.getWorlds().isMembersWorld();

        Area[] closestFive = Arrays.stream(BankLocation.values())
                .filter(f -> isMember || f.isF2P)
                .filter(f -> isDepositBoxAcceptable || !f.isDepositBox())
                .sorted(euclideanDistance)
                .limit(5)
                .map(m -> m.bankArea)
                .toArray(Area[]::new);

        Position closestPosition = LocationUtils.getClosestAreaByWebWalking(ctx, pos, closestFive);

        return Arrays.stream(BankLocation.values()).filter(f -> f.bankArea.contains(closestPosition)).findFirst().orElse(null);
    }

    public String getName() {
        return name;
    }

    public Area getBankArea() {
        return bankArea;
    }

    public boolean isF2P() {
        return isF2P;
    }

    public boolean isDepositBox() {
        return isDepositBox;
    }

    @Override
    public Position getPosition() {
        return bankArea.getRandomPosition();
    }
}
