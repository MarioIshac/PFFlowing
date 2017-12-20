package me.theeninja.pfflowing.gui;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;

import java.util.function.Function;

public enum RelativePosition {
    TOP(Bounds::getMinY),
    MIDDLE(bounds -> (bounds.getMaxY() + bounds.getMinY()) / 2),
    BOTTOM(Bounds::getMaxY);

    public static RelativePosition RELATIVE_POSITION = MIDDLE;

    private final Function<Bounds, Double> getYFunction;

    private static Bounds getBounds(Node node) {
        return node.localToScene(node.getBoundsInLocal());
    }

    private RelativePosition(Function<Bounds, Double> getYFunction) {
        this.getYFunction = getYFunction;
    }

    private double getX(Bounds bounds) {
        return bounds.getMinX();
    }

    private double getY(Bounds bounds) {
        return getGetYFunction().apply(bounds);
    }

    private Point2D getDefined(Node node) {
        Bounds bounds = getBounds(node);
        return new Point2D(getX(bounds), getY(bounds));
    }

    public Function<Bounds, Double> getGetYFunction() {
        return getYFunction;
    }
}
