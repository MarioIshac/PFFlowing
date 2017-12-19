package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.Side;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a speech that is produces on one's own side of the flow. DefensiveSpeech is in no way related
 * to the contract {@link Defensive}.
 */
public class DefensiveSpeech extends Speech {
    public static final DefensiveSpeech AFF_1 = new DefensiveSpeech(Side.AFFIRMATIVE, "Aff 1");
    public static final DefensiveSpeech AFF_2 = new DefensiveSpeech(Side.AFFIRMATIVE, "Aff 2");
    public static final DefensiveSpeech AFF_3 = new DefensiveSpeech(Side.AFFIRMATIVE, "Aff 3");
    public static final DefensiveSpeech AFF_4 = new DefensiveSpeech(Side.AFFIRMATIVE, "Aff 4");
    public static final DefensiveSpeech NEG_1 = new DefensiveSpeech(Side.NEGATION, "Neg 1");
    public static final DefensiveSpeech NEG_2 = new DefensiveSpeech(Side.NEGATION, "Neg 2");
    public static final DefensiveSpeech NEG_3 = new DefensiveSpeech(Side.NEGATION, "Neg 3");
    public static final DefensiveSpeech NEG_4 = new DefensiveSpeech(Side.NEGATION, "Neg 4");

    public static final List<DefensiveSpeech> DEFENSIVE_SPEECH_ORDER = Arrays.asList(AFF_1, NEG_1, AFF_2, NEG_2, AFF_3, NEG_3, AFF_4, NEG_4);

    private static int beginningIndex = 0;
    private static int endIndex = DEFENSIVE_SPEECH_ORDER.size() - 1;

    private DefensiveSpeech(Side side, String labelText) {
        super(side, labelText);
    }
}