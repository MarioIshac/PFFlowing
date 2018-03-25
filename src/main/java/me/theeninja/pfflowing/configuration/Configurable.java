package me.theeninja.pfflowing.configuration;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Configurable<T> {
    @Expose
    @SerializedName("descriptor")
    private final Descriptor descriptor;

    @Expose
    @SerializedName("value")
    private final ObjectProperty<T> value = new SimpleObjectProperty<>();

    Configurable(Descriptor descriptor, T defaultValue) {
        this.descriptor = descriptor;
        setValue(defaultValue);
    }

    public T getValue() {
        return value.get();
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public void setValue(T t) {
        value.setValue(t);
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }
}
