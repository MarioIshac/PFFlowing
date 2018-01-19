package me.theeninja.pfflowing.configuration;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Specification<T> {

    private final List<T> defaults;
    private final String identifier;
    private final Function<String, T> fromString;
    private T value;

    protected Specification(String identifier, Function<String, T> fromString) {
        this(Collections.emptyList(), identifier, fromString);
    }

    protected Specification(List<T> defaults, String identifier, Function<String, T> fromString) {
        this.defaults = defaults;
        this.identifier = identifier;
        this.fromString = fromString;
    }

    public List<T> getDefaults() {
        return defaults;
    }

    public T getTopDefault() {
        return getDefaults().get(0);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Function<String, T> getFromString() {
        return fromString;
    }
}
