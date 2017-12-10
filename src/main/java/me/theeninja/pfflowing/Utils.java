package me.theeninja.pfflowing;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Provides helper methods that assist in the functionality of this application.
 *
 * @author TheeNinja
 */
public final class Utils {

    /**
     * Generates a background of the given color.
     *
     * @param color
     * @return A background of the color specified.
     */
    public static Background generateBackgroundOfColor(Color color) {
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));

    }

    public static <T> T getCorrelatingController(String fxmlFile) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(Utils.class.getResource(fxmlFile));
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fxmlLoader.getController();
    }

    public static <T> ListChangeListener<? super T> generateListChangeListener(Consumer<T> addFunction, Consumer<T> removeFunction) {
        return change -> {
            while (change.next()) {
                if (change.wasAdded())
                    change.getAddedSubList().forEach(addFunction);
                else if (change.wasRemoved())
                    change.getRemoved().forEach(removeFunction);
            }
        };
    }
}
