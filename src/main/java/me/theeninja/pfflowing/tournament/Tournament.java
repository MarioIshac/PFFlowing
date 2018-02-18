package me.theeninja.pfflowing.tournament;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Tournament {
    private final ObservableList<Round> rounds = FXCollections.observableArrayList();
    private final ObjectProperty<Round> selectedRound = new SimpleObjectProperty<>();

    public ObservableList<Round> getRounds() {
        return rounds;
    }

    public Round getSelectedRound() {
        return selectedRound.get();
    }

    public ObjectProperty<Round> selectedRoundProperty() {
        return selectedRound;
    }
}