package me.theeninja.pfflowing.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import me.theeninja.pfflowing.SingleViewController;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class HelperController implements SingleViewController<TabPane>, Initializable {
    @FXML
    public TabPane helperView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Path appDocsPath = Paths.get("/appdocs");

        try {
            Stream<Path> subAppDocPathsStream = Files.walk(appDocsPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TabPane getCorrelatingView() {
        return helperView;
    }
}
