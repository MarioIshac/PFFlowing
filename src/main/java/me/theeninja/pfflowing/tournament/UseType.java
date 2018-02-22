package me.theeninja.pfflowing.tournament;

public enum UseType {
    NONE(false),
    TOURNAMENT(true),
    ROUND(true);

    private final boolean inUse;

    UseType(boolean inUse) {
        this.inUse = inUse;
    }

    public boolean isInUse() {
        return inUse;
    }
}
