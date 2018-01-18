package me.theeninja.pfflowing.gui;

import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import me.theeninja.pfflowing.configuration.LocalConfiguration;
import me.theeninja.pfflowing.configuration.Specification;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FlowCreator {
    public final List<Specification> specificationList;

    private Label getConfigurationLabel(Specification specification) {
        return new Label(specification.getIdentifier());
    }

    private ChoiceBox<String> getConfigurationChoiceBox(Specification specification) {
        ChoiceBox<String> choiceBox = new ChoiceBox<String>(FXCollections.<String>observableArrayList(specification.getDefaults()));
        return choiceBox;
    }

    public FlowCreator(LocalConfiguration localConfiguration) {
        Field[] specifcations = localConfiguration.getClass().getDeclaredFields();
        specificationList = Arrays.stream(specifcations).map(specificationField -> {
            try {
                specificationField.setAccessible(true);
                return (Specification) specificationField.get(localConfiguration);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
    }

    public void generateLocalConfigPopup() {
        Popup localConfigPopup = new Popup();

    }

    public void getSpecifications() {

    }
}
