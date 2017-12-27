package me.theeninja.pfflowing;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    public static <T> boolean isLastElement(List<T> list, T element) {
        return list.indexOf(element) == (list.size() - 1);
    }

    public static <T> T getNextElement(List<T> list, T element) {
        return Utils.getRelativeElement(list, element, 1);
    }

    public static <T> T getRelativeElement(List<T> list, T baseElement, int offset) {
        int baseIndex = list.indexOf(baseElement);

        if (offset == 0)
            return baseElement;

        int newIndex = 0;

        int beginningIndex = 0;
        int endIndex = list.size() - 1;

        // Indicates that we go forward in the array, potentially wrapping around the right end
        if (offset > 0) {
            newIndex = baseIndex + (offset % list.size());
            if (newIndex > endIndex)
                newIndex -= 8;
        }
        // Indicates that we go backwards in the array, potentially wrapping around the left end
        else {
            newIndex = baseIndex - (-offset % list.size());
            if (newIndex < beginningIndex)
                newIndex += 8;
        }

        if (newIndex > endIndex || newIndex < beginningIndex)
            throw new IllegalArgumentException("Base element provided, considering offset, will result in an illegal final index.");

        return list.get(newIndex);
    }

    public static <T> Optional<T> getPredicateSatisfier(Collection<T> collection, Predicate<T> predicate) {
        for (T element : collection) {
            if (predicate.test(element)) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    public static <T> List<T> getInstancesOfType(Collection<?> collection, Class<T> classRequired) {
        return collection.stream()
                .filter(classRequired::isInstance)
                .map(classRequired::cast)
                .collect(Collectors.toList());
    }
}
