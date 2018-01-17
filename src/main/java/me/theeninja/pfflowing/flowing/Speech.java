package me.theeninja.pfflowing.flowing;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.paint.Color;
import me.theeninja.pfflowing.gui.FlowingGridController;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;

import java.util.List;
import java.util.Map;

public abstract class Speech {

    public static final int SPEECH_SIZE = 8;

    private SimpleIntegerProperty proactiveStart = new SimpleIntegerProperty();
    private SimpleIntegerProperty proactiveEnd   = new SimpleIntegerProperty();

    private final Side side;
    private final String labelText;
    private final int gridPaneColumn;
    private final Map<Integer, FlowingRegion> children;

    Speech(Side side, String labelText, int flowingPaneColumn) {
        this.side = side;
        this.labelText = labelText;
        this.gridPaneColumn = flowingPaneColumn;
        this.children = FXCollections.observableHashMap();
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

    public int getGridPaneColumn() {
        return gridPaneColumn;
    }

    public Map<Integer, FlowingRegion> getChildren() {
        return children;
    }

    public List<ExtensionFlowingRegion> getExtensions() {
        return Utils.getInstancesOfType(getChildren().values(), ExtensionFlowingRegion.class);
    }

    public List<OffensiveFlowingRegion> getOffendors() {
        return Utils.getInstancesOfType(getChildren().values(), OffensiveFlowingRegion.class);
    }

    public List<DefensiveFlowingRegion> getConstructors() {
        return Utils.getInstancesOfType(getChildren().values(), DefensiveFlowingRegion.class);
    }

    public int getProactiveStart() {
        return proactiveStart.get();
    }

    public SimpleIntegerProperty proactiveStartProperty() {
        return proactiveStart;
    }

    public void setProactiveStart(int proactiveStart) {
        this.proactiveStart.set(proactiveStart);
    }

    public int getProactiveEnd() {
        return proactiveEnd.get();
    }

    public SimpleIntegerProperty proactiveEndProperty() {
        return proactiveEnd;
    }

    public void setProactiveEnd(int proactiveEnd) {
        this.proactiveEnd.set(proactiveEnd);
    }

    public int getNextAvailableRow() {
        System.out.println("DUDEEDED" + (getProactiveEnd() + 1));
        return getProactiveEnd() + 1;
    }
}