package nox.scripts.noxscape.core.interfaces;

import nox.scripts.noxscape.tasks.base.banking.BankLocation;
import org.osbot.rs07.api.map.Area;

public interface IBankable {
    BankLocation getBank();
    BankLocation getDepositBox();
}
