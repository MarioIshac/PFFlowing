package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.Side;

import java.util.Arrays;
import java.util.List;

public enum Speech {
    NEG_4(Side.NEGATION),
    AFF_4(Side.AFFIRMATIVE),
    NEG_3(Side.NEGATION),
    AFF_3(Side.AFFIRMATIVE),
    NEG_2(Side.NEGATION),
    AFF_2(Side.AFFIRMATIVE),
    NEG_1(Side.NEGATION),
    AFF_1(Side.AFFIRMATIVE);

    public static final List<Speech> SPEECH_ORDER = Arrays.asList(AFF_1, NEG_1, AFF_2, NEG_2, AFF_3, NEG_3, AFF_4, NEG_4);

    private static int beginningIndex = 0;
    private static int endIndex = SPEECH_ORDER.size() - 1;

    /**
     *
     * @param baseSpeech The speech to base the offset off of.
     * @param offset The number of speeches to move forwards (positive #) or backwards (negative #).
     * @return The speech relative to the base speech given the offset,
     * considering wrapping around the beginning or end.
     * @throws IllegalArgumentException
     */
    public static Speech getRelativeSpeech(Speech baseSpeech, int offset) throws IllegalArgumentException {
        int baseIndex = SPEECH_ORDER.indexOf(baseSpeech);

        if (offset == 0) {
            return baseSpeech;
        }

        int newIndex = 0;

        if (offset > 0) {
            newIndex = baseIndex + (offset % SPEECH_ORDER.size());
            if (newIndex > endIndex)
                newIndex -= 8;
        }
        else if (offset < 0) {
            newIndex = baseIndex - (-offset % SPEECH_ORDER.size());
            if (newIndex < beginningIndex)
                newIndex += 8;
        }

        if (newIndex > endIndex || newIndex < beginningIndex) {
            throw new IllegalArgumentException("Speech provided, considering offset, will result in an illegal final index.");
        }
        return SPEECH_ORDER.get(newIndex);
    }

    private final Side side;

    Speech(Side side) {
        this.side = side;
    }

    public Side getSide() {
        return side;
    }

}