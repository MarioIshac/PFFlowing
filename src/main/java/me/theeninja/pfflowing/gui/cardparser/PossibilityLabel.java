package me.theeninja.pfflowing.gui.cardparser;

import javafx.scene.control.Label;

public class PossibilityLabel<T> extends Label {
    private static final String BEFORE = "[%d] ";
    private static final String AFTER = "";
    private final T value;

    public PossibilityLabel(T value, int index) {
        super(
            String.format(BEFORE, index) +
            value.toString() +
            AFTER
        );
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
