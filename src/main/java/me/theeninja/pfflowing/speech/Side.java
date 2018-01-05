package me.theeninja.pfflowing.speech;

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
