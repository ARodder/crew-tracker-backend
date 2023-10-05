package net.aroder.TripTracker.models;

public enum TripStage {
    CREATED("created"),
    ASSIGNED("assigned"),
    ON_THE_WAY("on-the-way"),
    IN_PROGRESS("in-progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    DELAYED("delayed");

    private String state;

    TripStage(String state) {
        this.state = state;
    }
    public String getState() {
        return state;
    }
}
