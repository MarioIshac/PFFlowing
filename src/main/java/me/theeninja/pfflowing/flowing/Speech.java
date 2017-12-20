package me.theeninja.pfflowing.flowing;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import me.theeninja.pfflowing.Side;
import me.theeninja.pfflowing.Utils;
import me.theeninja.pfflowing.gui.Bindable;
import me.theeninja.pfflowing.gui.FlowingColumn;

public class Speech implements Bindable<FlowingColumn> {
    private final Side side;
    private final String labelText;
    private final ObservableList<FlowingRegion> flowingRegionList;
    private FlowingColumn bindedFlowingColumn;

    Speech(Side side, String labelText) {
        this.side = side;
        this.labelText = labelText;
        this.flowingRegionList = FXCollections.observableArrayList();
    }

    public Side getSide() {
        return side;
    }

    public String getLabelText() {
        return labelText;
    }

    public ObservableList<FlowingRegion> getFlowingRegionList() {
        return flowingRegionList;
    }

    @Override
    public void setBinded(FlowingColumn flowingColumn) {
        this.bindedFlowingColumn = flowingColumn;
        flowingRegionList.addListener(Utils.generateListChangeListener(
                getBinded().getChildren()::add,
                getBinded().getChildren()::remove
        ));
    }

    @Override
    public FlowingColumn getBinded() {
        return bindedFlowingColumn;
    }
}