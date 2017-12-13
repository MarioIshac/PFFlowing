package me.theeninja.pfflowing.gui;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class NavigatorController implements Initializable {
    private static NavigatorController fxmlInstance;

    public static NavigatorController getFxmlInstance() {
        return fxmlInstance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxmlInstance = this;
    }
}
