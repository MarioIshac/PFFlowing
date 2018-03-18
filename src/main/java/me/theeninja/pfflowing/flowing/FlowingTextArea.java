package me.theeninja.pfflowing.flowing;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
import me.theeninja.pfflowing.flowingregions.Card;

import java.util.List;

public class FlowingTextArea extends TextArea {
    private ObservableList<Card> addedCards = FXCollections.observableArrayList();
    private BooleanProperty cardSelectionMode = new SimpleBooleanProperty(false);

    public ObservableList<Card> getAddedCards() {
        return addedCards;
    }

    public void setAddedCards(ObservableList<Card> addedCards) {
        this.addedCards = addedCards;
    }

    public boolean isCardSelectionMode() {
        return cardSelectionMode.get();
    }

    public BooleanProperty cardSelectionModeProperty() {
        return cardSelectionMode;
    }

    public void setCardSelectionMode(boolean cardSelectionMode) {
        this.cardSelectionMode.set(cardSelectionMode);
    }
}
