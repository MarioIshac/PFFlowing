package me.theeninja.pfflowing.gui;

import javafx.animation.PauseTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import me.theeninja.pfflowing.StringSerializable;
import me.theeninja.pfflowing.configuration.Configuration;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.flowingregions.CharacterFormatting;
import me.theeninja.pfflowing.flowingregions.CharacterStyle;
import me.theeninja.pfflowing.flowingregions.DefensiveReasoning;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;
import me.theeninja.pfflowing.flowing.*;
import org.apache.commons.collections4.ListUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FlowingGrid extends GridPane {
    /**
     *
     */
    private Side side;

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public List<Node> getColumnChildren(int column) {
        return getChildren().stream().filter(node -> GridPane.getColumnIndex(node) == column).collect(Collectors.toList());
    }

    public List<Node> getRowChildren(int row) {
        return getChildren().stream().filter(node -> GridPane.getRowIndex(node) == row).collect(Collectors.toList());
    }

    public Optional<Node> getNode(int column, int row) {
        return getChildren().stream().filter(node -> GridPane.getColumnIndex(node) == column && GridPane.getRowIndex(node) == row ).findFirst();
    }

    public Optional<FlowingRegion> getRelativeFlowingRegion(FlowingRegion flowingRegion, int columnOffset, int rowOffset) {
        int baseColumn = GridPane.getColumnIndex(flowingRegion);
        int baseRow = GridPane.getRowIndex(flowingRegion);

        Optional<Node> possibleRelNode = getNode(baseColumn + columnOffset, baseRow + rowOffset);

        if (!possibleRelNode.isPresent())
            return Optional.empty();

        Node relNode = possibleRelNode.get();

        if (!(relNode instanceof FlowingRegion))
            return Optional.empty();

        return Optional.of((FlowingRegion) relNode);
    }

    public Optional<FlowingRegion> getLeft(FlowingRegion node) {
        return getRelativeFlowingRegion(node, -1, 0);
    }

    public Optional<FlowingRegion> getRight(FlowingRegion node) {
        return getRelativeFlowingRegion(node, 1, 0);
    }

    public Optional<FlowingRegion> getAbove(FlowingRegion node) {
        return getRelativeFlowingRegion(node, 0, -1);
    }

    public Optional<FlowingRegion> getBelow(FlowingRegion node) {
        return getRelativeFlowingRegion(node, 0, 1);
    }
}