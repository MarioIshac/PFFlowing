package me.theeninja.pfflowing.flowing;

import javafx.collections.MapChangeListener;
import me.theeninja.pfflowing.gui.FlowingGrid;
import me.theeninja.pfflowing.gui.FlowingGridController;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a speech that is produces on one's own side of the flow. DefensiveSpeech is in no way related
 * to the contract {@link Defensive}.
 */
public class DefensiveSpeech extends Speech {
    public DefensiveSpeech(Side side, String labelText, int column) {
        super(side, labelText, column);
    }
}