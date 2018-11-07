package me.theeninja.pfflowing.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import me.theeninja.pfflowing.SingleViewController;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
        try {
            URL appDocsURL = getClass().getResource("/appdocs");

            Path appDocsPath = Paths.get(appDocsURL.toURI());
            Stream<Path> subAppDocPathsStream = Files.walk(appDocsPath);

            subAppDocPathsStream.forEach(this::populateHelperView);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void populateHelperView(Path path) {
        try {
            Tab tab = new Tab(path.toString());
            byte[] htmlBytes = Files.readAllBytes(path);
            String html = new String(htmlBytes);

            WebView webView = new WebView();
            webView.getEngine().loadContent(html);

            tab.setContent(webView);
            getCorrelatingView().getTabs().add(tab);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TabPane getCorrelatingView() {
        return helperView;
    }
}
