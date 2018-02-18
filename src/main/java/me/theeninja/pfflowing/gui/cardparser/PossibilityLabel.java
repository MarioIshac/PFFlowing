package me.theeninja.pfflowing.gui.cardparser;

import javafx.scene.control.Label;

public class PossibilityLabel<T> extends Label {

    private final T value;
    private final String before;
    private final String after;
    private final Integer index;

    public PossibilityLabel(T value, String before, String after) {
        this(value, null, before, after);
    }

    public PossibilityLabel(T value, Integer index, String before, String after) {

        this.value = value;
        this.index = index;
        this.before = before;
        this.after = after;
        setText(
            String.format(getBefore(), index != null ? index : "") +
                value.toString() +
                getAfter()
        );
    }

    public T getValue() {
        return value;
    }

    public String getBefore() {
        return before;
    }

    public String getAfter() {
        return after;
    }
}
