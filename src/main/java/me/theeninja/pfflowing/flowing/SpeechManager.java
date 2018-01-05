package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.speech.Side;
import org.apache.commons.collections4.ListUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpeechManager {
    public final DefensiveSpeech AFF_1 = new DefensiveSpeech(Side.AFFIRMATIVE, "Aff 1");
    public final DefensiveSpeech AFF_2 = new DefensiveSpeech(Side.AFFIRMATIVE, "Aff 2");
    public final DefensiveSpeech AFF_3 = new DefensiveSpeech(Side.AFFIRMATIVE, "Aff 3");
    public final DefensiveSpeech AFF_4 = new DefensiveSpeech(Side.AFFIRMATIVE, "Aff 4");
    public final DefensiveSpeech NEG_1 = new DefensiveSpeech(Side.NEGATION, "Neg 1");
    public final DefensiveSpeech NEG_2 = new DefensiveSpeech(Side.NEGATION, "Neg 2");
    public final DefensiveSpeech NEG_3 = new DefensiveSpeech(Side.NEGATION, "Neg 3");
    public final DefensiveSpeech NEG_4 = new DefensiveSpeech(Side.NEGATION, "Neg 4");

    public final List<DefensiveSpeech> DEFENSIVE_SPEECH_ORDER = Arrays.asList(AFF_1, NEG_1, AFF_2, NEG_2, AFF_3, NEG_3, AFF_4, NEG_4);

    public final RefutationSpeech AT_AFF_1 = new RefutationSpeech(AFF_1, Side.NEGATION,"AT Aff 1");
    public final RefutationSpeech AT_AFF_2 = new RefutationSpeech(AFF_2, Side.NEGATION, "AT Aff 2");
    public final RefutationSpeech AT_AFF_3 = new RefutationSpeech(AFF_3, Side.NEGATION, "AT Aff 3");
    public final RefutationSpeech AT_AFF_4 = new RefutationSpeech(AFF_4, Side.NEGATION, "AT Aff 4");
    public final RefutationSpeech AT_NEG_1 = new RefutationSpeech(NEG_1, Side.AFFIRMATIVE, "AT Neg 1");
    public final RefutationSpeech AT_NEG_2 = new RefutationSpeech(NEG_2, Side.AFFIRMATIVE, "AT Neg 2");
    public final RefutationSpeech AT_NEG_3 = new RefutationSpeech(NEG_3, Side.AFFIRMATIVE, "AT Neg 3");
    public final RefutationSpeech AT_NEG_4 = new RefutationSpeech(NEG_4, Side.AFFIRMATIVE, "AT Neg 4");


    public final List<RefutationSpeech> REFUTATION_SPEECH_ORDER = Arrays.asList(AT_AFF_1, AT_NEG_1, AT_AFF_2, AT_NEG_2, AT_AFF_3, AT_NEG_3, AT_AFF_4, AT_NEG_4);

    public final List<Speech> ALL_SPEECHES = ListUtils.union(DEFENSIVE_SPEECH_ORDER, REFUTATION_SPEECH_ORDER);

    // DEFENSIVE_SPEECH_ORDER size should == REFUTATION_SPEECH_ORDER size (1:1 correspondance)
    public final int SPEECH_SIZE = DEFENSIVE_SPEECH_ORDER.size();

    public DefensiveSpeech getDefensiveSpeech(RefutationSpeech refutationSpeech) {
        return DEFENSIVE_SPEECH_ORDER.get(REFUTATION_SPEECH_ORDER.indexOf(refutationSpeech));
    }

    public RefutationSpeech getRefutationSpeech(DefensiveSpeech defensiveSpeech) {
        return REFUTATION_SPEECH_ORDER.get(DEFENSIVE_SPEECH_ORDER.indexOf(defensiveSpeech));
    }


}
