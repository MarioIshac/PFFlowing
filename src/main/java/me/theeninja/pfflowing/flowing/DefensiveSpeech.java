package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.speech.Side;

/**
 * Represents a speech that is produces on one's own side of the flow. DefensiveSpeech is in no way related
 * to the contract {@link Defensive}.
 */
public class DefensiveSpeech extends Speech {
    public DefensiveSpeech(Side side, String labelText, int column) {
        super(side, labelText, column);
    }
}