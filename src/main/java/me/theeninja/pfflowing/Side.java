package me.theeninja.pfflowing;

public enum Side {
    AFFIRMATIVE,
    NEGATION;

    public Side getOpposite() {
        if (this == AFFIRMATIVE)
            return NEGATION;
        else
            return AFFIRMATIVE;
    }
}
