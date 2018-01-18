package me.theeninja.pfflowing.configuration;

import me.theeninja.pfflowing.speech.Side;

public enum Speaker {
    FIRST_AFF(0, Side.AFFIRMATIVE),
    SECOND_AFF(1, Side.AFFIRMATIVE),
    FIRST_NEG(0, Side.NEGATION),
    SECOND_NEG(1, Side.AFFIRMATIVE);

    private final int speakerNumber;
    private final Side side;

    Speaker(int speakerNumber, Side side) {
        this.speakerNumber = speakerNumber;
        this.side = side;
    }

    public int getSpeakerNumber() {
        return speakerNumber;
    }

    public Side getSide() {
        return side;
    }
}
