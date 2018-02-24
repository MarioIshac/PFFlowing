package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.speech.Side;

public class RefutationSpeech extends Speech {
    private final DefensiveSpeech targetSpeech;

    public RefutationSpeech(DefensiveSpeech targetSpeech, Side side, String labelText, int flowingColumn) {
        super(side, labelText, flowingColumn);
        this.targetSpeech = targetSpeech;
    }

    public DefensiveSpeech getTargetSpeech() {
        return targetSpeech;
    }
}
