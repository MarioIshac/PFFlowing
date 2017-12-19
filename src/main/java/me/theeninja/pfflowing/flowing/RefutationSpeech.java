package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.Side;

import java.util.Arrays;
import java.util.List;

public class RefutationSpeech extends Speech {
    public static final RefutationSpeech AT_AFF_1 = new RefutationSpeech(DefensiveSpeech.AFF_1, Side.AFFIRMATIVE,"AT Aff 1");
    public static final RefutationSpeech AT_AFF_2 = new RefutationSpeech(DefensiveSpeech.AFF_2, Side.AFFIRMATIVE, "AT Aff 2");
    public static final RefutationSpeech AT_AFF_3 = new RefutationSpeech(DefensiveSpeech.AFF_3, Side.AFFIRMATIVE, "AT Aff 3");
    public static final RefutationSpeech AT_AFF_4 = new RefutationSpeech(DefensiveSpeech.AFF_4, Side.AFFIRMATIVE, "AT Aff 4");
    public static final RefutationSpeech AT_NEG_1 = new RefutationSpeech(DefensiveSpeech.NEG_1, Side.NEGATION, "AT Neg 1");
    public static final RefutationSpeech AT_NEG_2 = new RefutationSpeech(DefensiveSpeech.NEG_2, Side.NEGATION, "AT Neg 2");
    public static final RefutationSpeech AT_NEG_3 = new RefutationSpeech(DefensiveSpeech.NEG_3, Side.NEGATION, "AT Neg 3");
    public static final RefutationSpeech AT_NEG_4 = new RefutationSpeech(DefensiveSpeech.NEG_4, Side.NEGATION, "AT Neg 4");

    public static final List<RefutationSpeech> REFUTATION_SPEECH_ORDER = Arrays.asList(AT_AFF_1, AT_NEG_1, AT_AFF_2, AT_NEG_2, AT_AFF_3, AT_NEG_3, AT_AFF_4, AT_NEG_4);

    private final DefensiveSpeech targetSpeech;

    private RefutationSpeech(DefensiveSpeech targetSpeech, Side side, String labelText) {
        super(side, labelText);
        this.targetSpeech = targetSpeech;
    }

    public DefensiveSpeech getTargetSpeech() {
        return targetSpeech;
    }

    public static RefutationSpeech getRefutationSpeech(DefensiveSpeech defensiveSpeech) {
        for (RefutationSpeech refutationSpeech : RefutationSpeech.REFUTATION_SPEECH_ORDER) {
            if (defensiveSpeech == refutationSpeech.getTargetSpeech()) {
                return refutationSpeech;
            }
        }
        return null;
    }
}
