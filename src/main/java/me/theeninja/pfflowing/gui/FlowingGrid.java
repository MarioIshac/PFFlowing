package me.theeninja.pfflowing.gui;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.flowing.*;

import java.util.*;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

public class FlowingGrid extends GridPane {
    public static final int REF_COL_OFFSET = 1;
    public static final int EXT_COL_OFFSET = 2;

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

    public Optional<FlowingRegion> getFlowingRegion(int column, int row) {
        Optional<Node> optionalNode = getNode(column, row);
        if (optionalNode.isPresent() && optionalNode.get() instanceof FlowingRegion)
            return Optional.of((FlowingRegion) optionalNode.get());
        else
            return Optional.empty();
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

    public Optional<OffensiveFlowingRegion> getRefutation(FlowingRegion flowingRegion) {
        Optional<FlowingRegion> rightFlowingRegion = getFlowingRegion(FlowingGrid.getColumnIndex(flowingRegion) + REF_COL_OFFSET,
                FlowingGrid.getRowIndex(flowingRegion));

        if (rightFlowingRegion.isPresent() && rightFlowingRegion.get() instanceof OffensiveFlowingRegion)
            return Optional.of((OffensiveFlowingRegion) rightFlowingRegion.get());
        return Optional.empty();
    }

    public Optional<ExtensionFlowingRegion> getExtension(FlowingRegion flowingRegion) {
        Optional<FlowingRegion> rightFlowingRegion = getFlowingRegion(FlowingGrid.getColumnIndex(flowingRegion) + EXT_COL_OFFSET,
                FlowingGrid.getRowIndex(flowingRegion));

        if (rightFlowingRegion.isPresent() && rightFlowingRegion.get() instanceof ExtensionFlowingRegion)
            return Optional.of((ExtensionFlowingRegion) rightFlowingRegion.get());
        return Optional.empty();
    }

    public List<FlowingRegion> getPostLink(FlowingRegion flowingRegion) {
        return getChildren().stream().filter(node -> {
            // Verifies that this node is somehow part of the link
            boolean isSameRow = FlowingGrid.getRowIndex(node).equals(FlowingGrid.getRowIndex(flowingRegion));

            // Verifies that the node only appears post-deleted node in the link
            boolean isPastColumn = FlowingGrid.getColumnIndex(node) >= (FlowingGrid.getColumnIndex(flowingRegion));

            return node instanceof FlowingRegion && isSameRow && isPastColumn;
        }).map(FlowingRegion.class::cast).collect(Collectors.toList());
    }

    public List<FlowingRegion> getWholeLink(FlowingRegion flowingRegion) {
        return getChildren().stream().filter(node -> {
            // Verifies that this node is somehow part of the link
            boolean isSameRow = FlowingGrid.getRowIndex(node).equals(FlowingGrid.getRowIndex(flowingRegion));

            return node instanceof FlowingRegion && isSameRow;
        }).map(FlowingRegion.class::cast).collect(Collectors.toList());
    }
}