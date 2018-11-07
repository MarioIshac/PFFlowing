package me.theeninja.pfflowing.gui;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import me.theeninja.pfflowing.tournament.Round;

public class RoundTab extends Tab {
    private final Round round;

    RoundTab(Round round) {
        this.round = round;

        getRound().selectedControllerProperty().addListener(this::onSelectedControllerChanged);
        onSelectedControllerChanged(null, null, getRound().getSelectedController());

        setText(getRound().getRoundName());
    }

    private void onSelectedControllerChanged(ObservableValue<? extends FlowDisplayController> observable, FlowDisplayController oldValue, FlowDisplayController newValue) {
        System.out.println("content changed");
        setContent(newValue.getCorrelatingView());
    }

    public Round getRound() {
        return round;
    }
}
