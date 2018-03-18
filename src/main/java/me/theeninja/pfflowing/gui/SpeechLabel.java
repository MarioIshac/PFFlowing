package me.theeninja.pfflowing.gui;

import javafx.scene.control.Label;
import javafx.scene.control.SelectionModel;
import me.theeninja.pfflowing.flowing.Speech;
import me.theeninja.pfflowing.tournament.Round;

public class SpeechLabel extends Label {
    private final Speech speech;

    SpeechLabel(Speech speech) {
        super(speech.getLabelText());
        this.speech = speech;
    }

    public Speech getSpeech() {
        return speech;
    }
}
