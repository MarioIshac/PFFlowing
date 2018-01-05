package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.speech.Side;

import java.util.Arrays;
import java.util.List;

public class RefutationSpeech extends Speech {
    private final DefensiveSpeech targetSpeech;

    public RefutationSpeech(DefensiveSpeech targetSpeech, Side side, String labelText) {
        super(side, labelText);
        this.targetSpeech = targetSpeech;
    }

    public DefensiveSpeech getTargetSpeech() {
        return targetSpeech;
    }
}
