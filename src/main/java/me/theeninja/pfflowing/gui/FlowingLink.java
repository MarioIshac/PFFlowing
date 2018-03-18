package me.theeninja.pfflowing.gui;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.shape.Line;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.utils.Utils;

public class FlowingLink extends Line {

    private final int firstColumn;
    private final int secondColumn;
    private final int row;
    private final FlowDisplayController flowDisplayController;

    private void onStartBoundsChange(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
        double x = newValue.getMaxX();
        double y = (newValue.getMinY() + newValue.getMaxY()) / 2;
        setStartX(x);
        setStartY(y);
    }

    private void onEndBoundsChanged(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
        double x = newValue.getMaxX();
        double y = (newValue.getMinY() + newValue.getMaxY()) / 2;
        setEndX(x);
        setEndY(y);
    }


    public FlowingLink(int firstColumn, int secondColumn, int row, FlowDisplayController flowDisplayController) {
        this.firstColumn = firstColumn;
        this.secondColumn = secondColumn;
        this.row = row;
        this.flowDisplayController = flowDisplayController;

        FlowingRegion start = flowDisplayController.flowGrid.getFlowingRegion(firstColumn, row).get();
        FlowingRegion end = flowDisplayController.flowGrid.getFlowingRegion(secondColumn, row).get();

        start.boundsInParentProperty().addListener(this::onStartBoundsChange);
        end.boundsInParentProperty().addListener(this::onEndBoundsChanged);
    }
}