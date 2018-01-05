package me.theeninja.pfflowing.gui;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.shape.Line;
import me.theeninja.pfflowing.SpeechListSpecific;
import me.theeninja.pfflowing.configuration.Configuration;
import me.theeninja.pfflowing.flowing.FlowingRegion;

import java.util.function.Supplier;

public class FlowingLink extends Line implements SpeechListSpecific {
    private final int startID;
    private final int endID;
    private final SpeechList belongingSpeechList;

    public void rebindProperties() {
        FlowingRegion start = FlowingRegion.getFlowingRegion(getStartID());
        FlowingRegion end = FlowingRegion.getFlowingRegion(getEndID());

        Bounds starterBounds = start.localToScene(start.getLayoutBounds());
        double startX = starterBounds.getMaxX() + Configuration.ARROW_MARGIN;
        double startY = (starterBounds.getMinY() + starterBounds.getMaxY()) / 2;

        Bounds finishBounds = end.localToScene(end.getLayoutBounds());
        double finishX = finishBounds.getMinX() - Configuration.ARROW_MARGIN;
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
                () -> start.localToScene(start.getLayoutBounds()).getMaxX() + Configuration.ARROW_MARGIN,
                start.layoutBoundsProperty()));

        startYProperty().bind(generateDoubleBinding(() -> {
                    Bounds localStarterBounds = start.localToScene(start.getLayoutBounds());
                    return (localStarterBounds.getMinY() + localStarterBounds.getMaxY()) / 2;
                },
                start.layoutBoundsProperty()));

        endXProperty().bind(generateDoubleBinding(
                () -> end.localToScene(end.getLayoutBounds()).getMinX() - Configuration.ARROW_MARGIN,
                end.layoutBoundsProperty()));

        endYProperty().bind(generateDoubleBinding(
                () -> {
                    Bounds localFinishBounds = end.localToScene(end.getLayoutBounds());
                    return (localFinishBounds.getMinY() + localFinishBounds.getMaxY()) / 2;
                },
                start.layoutBoundsProperty()));

        visibleProperty().bind(new BooleanBinding() {
            {
                super.bind(FlowingGridController.getFXMLInstance().getSpeechListManager().selectedSpeechListProperty());
            }

            @Override
            protected boolean computeValue() {
                return getBelongingSpeechList() == FlowingGridController.getFXMLInstance().getSpeechListManager().getSelectedSpeechList();
            }
        });
    }

    public FlowingLink(FlowingRegion start, FlowingRegion finish) {
        this.startID = start.getID();
        this.endID = finish.getID();
        this.belongingSpeechList = FlowingGridController.getFXMLInstance().getSpeechListManager().getSpeechList(start.getFlowingColumn().getBinded());

        rebindProperties();
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

    @Override
    public SpeechList getBelongingSpeechList() {
        return belongingSpeechList;
    }

    public int getStartID() {
        return startID;
    }

    public int getEndID() {
        return endID;
    }
}
