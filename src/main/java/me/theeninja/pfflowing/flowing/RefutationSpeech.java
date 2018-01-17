package me.theeninja.pfflowing.flowing;

import javafx.collections.MapChangeListener;
import me.theeninja.pfflowing.gui.FlowingGrid;
import me.theeninja.pfflowing.gui.FlowingGridController;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;

import java.util.Arrays;
import java.util.List;

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
