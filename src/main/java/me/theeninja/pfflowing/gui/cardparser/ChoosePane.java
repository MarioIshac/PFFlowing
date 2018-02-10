package me.theeninja.pfflowing.gui.cardparser;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class ChoosePane<T> extends FlowPane {
    private ObjectProperty<T> selected = new SimpleObjectProperty<>();

    public ChoosePane() {
        super();
    }

    public void setSelected(T value) {
        selected.set(value);
    }

    public void setSelected(int index) {
        PossibilityLabel<T> possibilityLabel = (PossibilityLabel<T>) getChildren().get(index);
        selected.set(possibilityLabel.getValue());
    }

    public T getSelected() {
        return selected.get();
    }

    public ObjectProperty<T> selectedProperty() {
        return selected;
    }

    public void add(PossibilityLabel<T> possibilityLabel) {
        this.getChildren().add(possibilityLabel);
    }
}
