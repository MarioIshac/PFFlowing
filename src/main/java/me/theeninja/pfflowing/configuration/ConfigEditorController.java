package me.theeninja.pfflowing.configuration;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import me.theeninja.pfflowing.SingleViewController;
import org.controlsfx.control.PropertySheet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
        this.addConfigurationItems();
    }

    private void addConfigurationItems() {
        // Configuration.class is final, therefore no need to go over inherited fields (which are non existent)
        try {
            for (Field field : Configuration.class.getDeclaredFields()) {
                Class<?> fieldType = field.getType();

                if (Configurable.class.isAssignableFrom(fieldType)) {
                    addConfigurationItem(field);
                }
            }
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | SecurityException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void addConfigurationItem(Field field) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        field.setAccessible(true);

        Constructor<ConfigurationItem> configurationItemConstructor = ConfigurationItem.class.getConstructor(Configurable.class);
        configurationItemConstructor.setAccessible(true);

        Configurable<?> configurable = (Configurable<?>) field.get(getConfiguration());

        ConfigurationItem<?> configurationItem = configurationItemConstructor.newInstance(configurable);

        propertySheet.getItems().add(configurationItem);
    }

    @Override
    public PropertySheet getCorrelatingView() {
        return propertySheet;
    }
}
