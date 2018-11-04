package me.theeninja.pfflowing.speech;

public enum Side {
    AFFIRMATIVE((byte) 0),
    NEGATION((byte) 1);

    private final byte representation;

    Side(byte representation) {
        this.representation = representation;
    }

    public Side getOpposite() {
        if (this == AFFIRMATIVE)
            return NEGATION;
        else
            return AFFIRMATIVE;
    }

    public byte getRepresentation() {
        return representation;
    }

    public static Side getSide(byte sideRepresentation) {
        for (Side side : Side.values()) {
            if (side.getRepresentation() == sideRepresentation) {
                return side;
            }
        }

        return null;
    }
}
