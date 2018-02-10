package me.theeninja.pfflowing.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.configuration.GlobalConfiguration;
import me.theeninja.pfflowing.utils.Pair;
import me.theeninja.pfflowing.utils.Utils;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class NotificationDisplayController implements SingleViewController<HBox>, Initializable {
    @FXML public HBox notificationBox;

    public static NotificationDisplayController getFXMLInstance() {
        return fxmlInstance;
    }

    public void reset() {
        getCorrelatingView().getChildren().clear();
    }

    public void info(String text) {
        notify(text, Level.INFO);
    }

    public void warn(String text) {
        notify(text, Level.WARNING);
    }

    public void error(String text) {
        notify(text, Level.SEVERE);
    }

    private void notify(String text, Level level) {
        Label notificationLabel = new Label(text);
        notificationLabel.prefWidthProperty().bind(getCorrelatingView().widthProperty());

        Pair<Color, Color> colorPair = GlobalConfiguration.LEVEL_COLORS.get(level);
        notificationLabel.setTextFill(colorPair.getFirst());
        notificationLabel.setBackground(Utils.generateBackgroundOfColor(colorPair.getSecond()));

        this.reset();
        getCorrelatingView().getChildren().add(notificationLabel);
    }

    /**
     * Provides the one, main view that is represented by this controller.
     *
     * @return The one, main view correlating to the controller.
     */
    @Override
    public HBox getCorrelatingView() {
        return notificationBox;
    }

    private static NotificationDisplayController fxmlInstance;

    /**
     * Called to start a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fxmlInstance = this;
    }
}
