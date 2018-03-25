package me.theeninja.pfflowing.configuration;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import me.theeninja.pfflowing.SingleViewController;
import org.controlsfx.control.PropertySheet;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConfigEditorController implements SingleViewController<PropertySheet>, Initializable {

    private final Configuration configuration;

    @FXML
    public PropertySheet propertySheet;

    public ConfigEditorController(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ConfigurationItem<Color> affColorItem = new ConfigurationItem<>(getConfiguration().getAffColor());
        ConfigurationItem<Color> negColorItem = new ConfigurationItem<>(getConfiguration().getNegColor());
        ConfigurationItem<Color> backgroundColorItem = new ConfigurationItem<>(getConfiguration().getBackgroundColor());
        ConfigurationItem<Font> fontItem = new ConfigurationItem<Font>(getConfiguration().getFont());
        propertySheet.getItems().addAll(affColorItem, negColorItem, backgroundColorItem, fontItem);
    }

    @Override
    public PropertySheet getCorrelatingView() {
        return propertySheet;
    }

    private class ConfigurationItem<T> implements PropertySheet.Item {

        private final Configurable<T> configurable;

        ConfigurationItem(Configurable<T> configurable) {
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
}
