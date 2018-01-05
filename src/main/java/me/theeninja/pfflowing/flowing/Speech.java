package me.theeninja.pfflowing.flowing;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import me.theeninja.pfflowing.gui.FlowingGrid;
import me.theeninja.pfflowing.gui.FlowingPane;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;
import me.theeninja.pfflowing.gui.FlowingColumn;

import java.util.ArrayList;
import java.util.List;

public class Speech {
    private final Side side;
    private final String labelText;
    private final FlowingGrid flowingGrid;
    private final int gridPaneColumn;
    private final List<FlowingRegion> flowingRegionList;

    Speech(Side side, String labelText, FlowingGrid flowingGrid, int flowingPaneColumn) {
        this.side = side;
        this.labelText = labelText;
        this.flowingGrid = flowingGrid;
        this.gridPaneColumn = flowingPaneColumn;
        this.flowingRegionList = new ArrayList<>();
    }

    public Color getColor() {
        return getSide() == Side.AFFIRMATIVE ? Color.BLACK : Color.RED;
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

    public int getGridPaneColumn() {
        return gridPaneColumn;
    }

    public FlowingGrid getFlowingGrid() {
        return flowingGrid;
    }
}