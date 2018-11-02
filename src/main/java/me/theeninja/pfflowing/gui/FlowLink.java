package me.theeninja.pfflowing.gui;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.shape.Line;
import me.theeninja.pfflowing.flowing.FlowingRegion;

import java.util.ArrayList;
import java.util.List;

public class FlowLink {
    private static final double ARROW_MARGIN = 5;
    private static final int X_INDEX = 0;
    private static final int Y_INDEX = 1;

    private final List<Line> lines = new ArrayList<>();
    private final Line mainLine = new Line();

    private Line getMainLine() {
        return this.mainLine;
    }

    private static final double ARROW_HEAD_WIDTH = 5;
    private static final double ARROW_HEAD_HEIGHT = 5;

    private final FlowingRegion first;
    private final FlowingRegion second;

    FlowLink(FlowingRegion first, FlowingRegion second) {
        this.first = first;
        this.second = second;

        getFirst().boundsInParentProperty().addListener(this::onFirstBoundsChange);
        getSecond().boundsInParentProperty().addListener(this::onSecondBoundsChange);

        Line topArrowHead = new Line();
        topArrowHead.startXProperty().bind(topArrowHead.endXProperty().subtract(ARROW_HEAD_WIDTH));
        topArrowHead.startYProperty().bind(topArrowHead.endYProperty().add(ARROW_HEAD_HEIGHT / 2));
        topArrowHead.endXProperty().bind(getMainLine().endXProperty());
        topArrowHead.endYProperty().bind(getMainLine().endYProperty());

        Line bottomArrowHead = new Line();
        bottomArrowHead.startXProperty().bind(bottomArrowHead.endXProperty().subtract(ARROW_HEAD_WIDTH));
        bottomArrowHead.startYProperty().bind(bottomArrowHead.endYProperty().subtract(ARROW_HEAD_HEIGHT / 2));
        bottomArrowHead.endXProperty().bind(getMainLine().endXProperty());
        bottomArrowHead.endYProperty().bind(getMainLine().endYProperty());

        getLines().add(topArrowHead);
        getLines().add(bottomArrowHead);
        getLines().add(getMainLine());

        // Force update in order to make arrow position correct rather than at (0, 0)
        // this is needed because because arrow changes position when bounds CHANGE, excluding
        // first position of arrow
        onFirstBoundsChange(null, null, getFirst().getBoundsInParent());
        onSecondBoundsChange(null, null, getSecond().getBoundsInParent());

        getLines().forEach(line -> line.setManaged(false));
    }

    public FlowingRegion getFirst() {
        return first;
    }

    public FlowingRegion getSecond() {
        return second;
    }

    public double[] getCoordinates(Bounds newBounds, boolean isLeft) {
        double x = isLeft ? newBounds.getMaxX() + ARROW_MARGIN :
                            newBounds.getMinX() - ARROW_MARGIN;

        double minY = newBounds.getMinY();
        double maxY = newBounds.getMaxY();
        double averageY = (minY + maxY) / 2;

        double[] coordinateArray = new double[2];
        coordinateArray[X_INDEX] = x;
        coordinateArray[Y_INDEX] = averageY;

        return coordinateArray;
    }

    private void onFirstBoundsChange(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
        double[] coordinateArray = getCoordinates(newValue, true);
        double x = coordinateArray[X_INDEX];
        double y = coordinateArray[Y_INDEX];

        getMainLine().setStartX(x);
        getMainLine().setStartY(y);
    }

    private void onSecondBoundsChange(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
        double[] coordinateArray = getCoordinates(newValue, false);
        double x = coordinateArray[X_INDEX];
        double y = coordinateArray[Y_INDEX];

        getMainLine().setEndX(x);
        getMainLine().setEndY(y);
    }

    public List<Line> getLines() {
        return lines;
    }
}