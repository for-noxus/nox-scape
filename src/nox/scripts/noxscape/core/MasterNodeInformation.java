package nox.scripts.noxscape.core;

import nox.scripts.noxscape.core.enums.Duration;
import nox.scripts.noxscape.core.enums.Frequency;
import nox.scripts.noxscape.core.enums.MasterNodeType;

import java.util.Objects;

public class MasterNodeInformation {

    private final String friendlyName;
    private final String description;
    private final Frequency frequency;
    private final Duration duration;
    private final MasterNodeType masterNodeType;

    public MasterNodeInformation(String friendlyName, String description, Frequency frequency, Duration duration, MasterNodeType masterNodeType) {
        this.friendlyName = friendlyName;
        this.description = description;
        this.frequency = frequency;
        this.duration = duration;
        this.masterNodeType = masterNodeType;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public String getDescription() {
        return description;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public Duration getDuration() {
        return duration;
    }

    public MasterNodeType getMasterNodeType() {
        return masterNodeType;
    }

    @Override
    public String toString() {
        return "MasterNodeInformation{" +
                "friendlyName='" + friendlyName + '\'' +
                ", description='" + description + '\'' +
                ", frequency=" + frequency +
                ", duration=" + duration +
                ", masterNodeType=" + masterNodeType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MasterNodeInformation that = (MasterNodeInformation) o;
        return Objects.equals(friendlyName, that.friendlyName) &&
                frequency == that.frequency &&
                duration == that.duration &&
                masterNodeType == that.masterNodeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(friendlyName, frequency, duration, masterNodeType);
    }
}
