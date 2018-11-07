package me.theeninja.pfflowing.gui.cardparser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.SingleViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class BlocksParserConfigController implements SingleViewController<VBox>, Initializable {
    @FXML
    public VBox configBox;

    @FXML
    public VBox criteriaBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public VBox getCorrelatingView() {
        return this.configBox;
    }

    public void addNewCriteria(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = new FXMLLoader();
    }
}
