package me.theeninja.pfflowing.gui;

import me.theeninja.pfflowing.flowing.DefensiveSpeech;
import me.theeninja.pfflowing.flowing.RefutationSpeech;
import me.theeninja.pfflowing.utils.Pair;

public class SpeechPair extends Pair<DefensiveSpeech, RefutationSpeech> {
    public SpeechPair(DefensiveSpeech defensiveSpeech, RefutationSpeech refutationSpeech) {
        super(defensiveSpeech, refutationSpeech);
    }
}
