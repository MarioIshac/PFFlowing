package me.theeninja.pfflowing.gui;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import me.theeninja.pfflowing.flowing.*;

import java.util.*;
import java.util.concurrent.Flow;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlowGrid extends GridPane {
    public static final int REF_COL_OFFSET = 1;
    public static final int EXT_COL_OFFSET = 2;

    private static boolean isOnGrid(Node node) {
        Integer columnIndex = FlowGrid.getColumnIndex(node);
        Integer rowIndex = FlowGrid.getRowIndex(node);

        return columnIndex != null && rowIndex != null;
    }
    public List<Node> getColumnChildren(int column) {
        return getChildren().stream()
                .filter(FlowGrid::isOnGrid)
                .filter(node -> GridPane.getColumnIndex(node) == column).collect(Collectors.toList());
    }

    public List<Node> getRowChildren(int row) {
        return getChildren().stream()
                .filter(FlowGrid::isOnGrid)
                .filter(node -> GridPane.getRowIndex(node) == row).collect(Collectors.toList());
    }

    public Optional<Node> getNode(int column, int row) {
        return getChildren().stream()
            .filter(FlowGrid::isOnGrid)
            .filter(node -> {
                Integer columnIndex = FlowGrid.getColumnIndex(node);
                Integer rowIndex = FlowGrid.getRowIndex(node);

                return columnIndex == column && rowIndex == row;
            }).findFirst();
    }

    public Optional<FlowingRegion> getFlowingRegion(int column, int row) {
        Optional<Node> node = getNode(column, row);

        return node
                .filter(FlowingRegion.class::isInstance)
                .map(FlowingRegion.class::cast);
    }

    public Optional<FlowingRegion> getRelativeFlowingRegion(FlowingRegion flowingRegion, Direction direction) {
        int column = FlowGrid.getColumnIndex(flowingRegion);
        int row = FlowGrid.getRowIndex(flowingRegion);

        return getRelativeFlowingRegion(column, row, direction);
    }

    public Optional<FlowingRegion> getRelativeFlowingRegion(int baseColumn, int baseRow, Direction direction) {
        switch (direction) {
            case LEFT:
                for (int column = baseColumn - 1; column >= 0; column--) {
                    Optional<FlowingRegion> optionalFlowingRegion = getFlowingRegion(column, baseRow);
                    if (optionalFlowingRegion.isPresent())
                        return optionalFlowingRegion;
                }
                break;
            case RIGHT:
                for (int column = baseColumn + 1; column < getColumnCount(); column++) {
                    Optional<FlowingRegion> optionalFlowingRegion = getFlowingRegion(column, baseRow);
                    if (optionalFlowingRegion.isPresent())
                        return optionalFlowingRegion;
                }
                break;
            case UP: {
                for (int row = baseRow - 1; row >= 0; row--) {
                    Optional<FlowingRegion> optionalFlowingRegion = getFlowingRegion(baseColumn, row);
                    if (optionalFlowingRegion.isPresent())
                        return optionalFlowingRegion;
                }
                break;
            }
            case DOWN: {
                for (int row = baseRow + 1; row < getRowCount(); row++) {
                    Optional<FlowingRegion> optionalFlowingRegion = getFlowingRegion(baseColumn, row);
                    if (optionalFlowingRegion.isPresent())
                        return optionalFlowingRegion;
                }
                break;
            }

            default: return Optional.empty();
        }
        return Optional.empty();
    }

    public Optional<FlowingRegion> getLeft(FlowingRegion node) {
        return getRelativeFlowingRegion(node, Direction.LEFT);
    }

    public Optional<FlowingRegion> getRight(FlowingRegion node) {
        return getRelativeFlowingRegion(node, Direction.RIGHT);
    }

    public Optional<FlowingRegion> getAbove(FlowingRegion node) {
        return getRelativeFlowingRegion(node, Direction.UP);
    }

    public Optional<FlowingRegion> getBelow(FlowingRegion node) {
        return getRelativeFlowingRegion(node, Direction.DOWN);
    }

    public Optional<FlowingRegion> getRefutation(FlowingRegion flowingRegion) {
        Optional<FlowingRegion> rightFlowingRegion = getFlowingRegion(
                FlowGrid.getColumnIndex(flowingRegion) + REF_COL_OFFSET,
                FlowGrid.getRowIndex(flowingRegion));

        return rightFlowingRegion
                .map(FlowingRegion.class::cast)
                .filter(FlowingRegion::isOffensive);
    }

    public Optional<FlowingRegion> getExtension(FlowingRegion flowingRegion) {
        Optional<FlowingRegion> rightFlowingRegion = getFlowingRegion(
                FlowGrid.getColumnIndex(flowingRegion) + EXT_COL_OFFSET,
                FlowGrid.getRowIndex(flowingRegion));

        return rightFlowingRegion
                .map(FlowingRegion.class::cast)
                .filter(FlowingRegion::isExtension);
    }

    public List<FlowingRegion> getPostLink(FlowingRegion flowingRegion) {
        return getChildren().stream().filter(FlowGrid::isOnGrid).filter(node -> {
            // Verifies that this node is somehow part of the link
            boolean isSameRow = FlowGrid.getRowIndex(node).equals(FlowGrid.getRowIndex(flowingRegion));

            // Verifies that the node only appears post-deleted node in the link
            boolean isPastColumn = FlowGrid.getColumnIndex(node) >= (FlowGrid.getColumnIndex(flowingRegion));

            return node instanceof FlowingRegion && isSameRow && isPastColumn;
        }).map(FlowingRegion.class::cast).collect(Collectors.toList());
    }

    public List<FlowingRegion> getWholeLink(FlowingRegion flowingRegion) {
        return getChildren().stream()
                .filter(FlowGrid::isOnGrid)
                .filter(node -> {
                    // Verifies that this node is somehow part of the link
                    boolean isSameRow = FlowGrid.getRowIndex(node).equals(FlowGrid.getRowIndex(flowingRegion));

                    return node instanceof FlowingRegion && isSameRow;
                })
                .map(FlowingRegion.class::cast)
                .collect(Collectors.toList());
    }
}