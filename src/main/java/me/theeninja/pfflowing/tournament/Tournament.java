package me.theeninja.pfflowing.tournament;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

public class Tournament {
    private String name;
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

    public void setSelectedRound(Round round) {
        this.selectedRound.set(round);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tournament(String name) {
        this.name = name;
    }
}