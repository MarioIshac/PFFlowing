package me.theeninja.pfflowing.gui;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.shape.Line;
import me.theeninja.pfflowing.flowing.FlowingRegion;

import java.util.function.Supplier;

public class FlowingLink extends Line {

    private final int firstColumn;
    private final int secondColumn;
    private final int row;
    private final FlowDisplayController flowDisplayController;

    public void rebindProperties() {
        FlowingRegion start = flowDisplayController.flowGrid.getFlowingRegion(firstColumn, row).get();
        FlowingRegion end = flowDisplayController.flowGrid.getFlowingRegion(secondColumn, row).get();


        Bounds starterBounds = start.localToScene(start.getLayoutBounds());
        double startX = starterBounds.getMaxX(); //+ Configuration.ARROW_MARGIN;
        double startY = (starterBounds.getMinY() + starterBounds.getMaxY()) / 2;

        Bounds finishBounds = end.localToScene(end.getLayoutBounds());
        double finishX = finishBounds.getMinX(); // - Configuration.ARROW_MARGIN;
        double finishY = (finishBounds.getMinY() + finishBounds.getMaxY()) / 2;

        startXProperty().unbind();
        startYProperty().unbind();
        endXProperty().unbind();
        endYProperty().unbind();

        setStartX(startX);
        setStartY(startY);
        setEndX(finishX);
        setEndY(finishY);

        // Save this for later: https://stackoverflow.com/questions/44122895/javafx-bind-to-nested-position

        startXProperty().bind(generateDoubleBinding(
                () -> start.localToScene(start.getLayoutBounds()).getMaxX() /*+ Configuration.ARROW_MARGIN*/,
                start.layoutBoundsProperty()));

        startYProperty().bind(generateDoubleBinding(() -> {
                    Bounds localStarterBounds = start.localToScene(start.getLayoutBounds());
                    return (localStarterBounds.getMinY() + localStarterBounds.getMaxY()) / 2;
                },
                start.layoutBoundsProperty()));

        endXProperty().bind(generateDoubleBinding(
                () -> end.localToScene(end.getLayoutBounds()).getMinX() /*- Configuration.ARROW_MARGIN*/,
                end.layoutBoundsProperty()));

        endYProperty().bind(generateDoubleBinding(
                () -> {
                    Bounds localFinishBounds = end.localToScene(end.getLayoutBounds());
                    return (localFinishBounds.getMinY() + localFinishBounds.getMaxY()) / 2;
                },
                start.layoutBoundsProperty()));
    }

    public FlowingLink(int firstColumn, int secondColumn, int row, FlowDisplayController flowDisplayController) {
        this.firstColumn = firstColumn;
        this.secondColumn = secondColumn;
        this.row = row;
        this.flowDisplayController = flowDisplayController;
    }

    private static DoubleBinding generateDoubleBinding(Supplier<Double> computationFunction, ObservableValue<?>... observableValue) {
        return new DoubleBinding() {
            {
                super.bind(observableValue);
            }

            @Override
            protected double computeValue() {
                return computationFunction.get();
            }
        };
    }
}