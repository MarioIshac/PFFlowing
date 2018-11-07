package me.theeninja.pfflowing.configuration;

import javafx.beans.value.ObservableValue;
import me.theeninja.pfflowing.configuration.Configurable;
import org.controlsfx.control.PropertySheet;

import java.util.Optional;

public class ConfigurationItem<T> implements PropertySheet.Item {

    private final Configurable<T> configurable;

    public ConfigurationItem(Configurable<T> configurable) {
        this.configurable = configurable;
    }

    @Override
    public Class<?> getType() {
        return getValue().getClass();
    }

    @Override
    public String getCategory() {
        return getConfigurable().getDescriptor().getGroup();
    }

    @Override
    public String getName() {
        return getConfigurable().getDescriptor().getName();
    }

    @Override
    public String getDescription() {
        return getConfigurable().getDescriptor().getDescription();
    }

    @Override
    public T getValue() {
        return getConfigurable().getValue();
    }

    @Override
    public void setValue(Object object) {
        if (object.getClass() != getType())
            throw new IllegalArgumentException("object not instance of " + getType().getSimpleName());

        @SuppressWarnings("unchecked")
        T value = (T) object;

        getConfigurable().setValue(value);
    }

    @Override
    public Optional<ObservableValue<?>> getObservableValue() {
        return Optional.of(getConfigurable().valueProperty());
    }

    public Configurable<T> getConfigurable() {
        return configurable;
    }
}