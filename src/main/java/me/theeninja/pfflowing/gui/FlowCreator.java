package me.theeninja.pfflowing.gui;

import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import me.theeninja.pfflowing.configuration.LocalConfiguration;
import me.theeninja.pfflowing.configuration.Specification;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FlowCreator {
    private final LocalConfiguration localConfiguration;

    private Label getConfigurationLabel(Specification specification) {
        return new Label(specification.getIdentifier());
    }

    private <T> ChoiceBox<T> getConfigurationChoiceBox(Specification<T> specification) {
        return new ChoiceBox<>(FXCollections.observableList(specification.getDefaults()));
    }

    private <T> TextField getConfigurationTextField(Specification<T> specification) {
        return new TextField();
    }

    public FlowCreator(LocalConfiguration localConfiguration) {
        this.localConfiguration = localConfiguration;
    }

    public void generateLocalConfigPopup() {
        Popup localConfigPopup = new Popup();
        for (Specification<?> specification : getLocalConfiguration().ALL) {
            HBox specificationHBox = new HBox();
            specificationHBox.getChildren().add(getConfigurationChoiceBox(specification));

            List<?> defaults = specification.getDefaults();
            // indicates that the user must type the configuration value
            if (specification.getDefaults().isEmpty()){
                TextField textField = getConfigurationTextField();
                textField.setOnAction(actionEvent -> {
                    specification.setValue();
                });
                specificationHBox.getChildren().add();
            }
            // indicates that the user must select the configuration value
            else {

            }
        }
    }

    public void getSpecifications() {

    }

    public LocalConfiguration getLocalConfiguration() {
        return localConfiguration;
    }
}
