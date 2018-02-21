package me.theeninja.pfflowing.gui;

import javafx.scene.control.Tab;
import me.theeninja.pfflowing.tournament.Round;

public class RoundTab extends Tab {
    private final Round round;

    public RoundTab(Round round) {
        this.round = round;

        getRound().selectedControllerProperty().addListener((observable, oldValue, newValue) -> {
            setContent(newValue.getCorrelatingView());
        });

        textProperty().bind(round.nameProperty());
    }

    public Round getRound() {
        return round;
    }
}
