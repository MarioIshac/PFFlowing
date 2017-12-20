package me.theeninja.pfflowing.gui;

import javafx.scene.layout.HBox;
import me.theeninja.pfflowing.flowing.DefensiveSpeech;

public class FlowingColumns extends HBox implements Bindable<SpeechListManager> {
    private SpeechListManager bindedSpeechListManager;

    public void display(SpeechList speechList) {
        boolean firstTime = false;

        if (getChildren().size() == 0)
            firstTime = true;

        for (int i = 0; i < DefensiveSpeech.DEFENSIVE_SPEECH_ORDER.size(); i++)
            if (firstTime)
                getChildren().add(speechList.getSpeeches().get(i).getBinded());
            else
                getChildren().set(i, speechList.getSpeeches().get(i).getBinded());
    }

    @Override
    public void setBinded(SpeechListManager speechListManager) {
        this.bindedSpeechListManager = speechListManager;
    }

    @Override
    public SpeechListManager getBinded() {
        return bindedSpeechListManager;
    }
}