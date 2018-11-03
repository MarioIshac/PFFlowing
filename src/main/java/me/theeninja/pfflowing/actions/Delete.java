package me.theeninja.pfflowing.actions;

import me.theeninja.pfflowing.actions.Action;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.gui.FlowGrid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Delete extends FlowAction {
    private List<FlowingRegion> deletedFlowingRegions;

    private final Map<FlowingRegion, List<Integer>> updateMap;

    public Delete(FlowDisplayController flowDisplayController, List<FlowingRegion> flowingRegions) {
        super(flowDisplayController);

        // Remove duplicates, as they are a possibility. An example to demonstrate:
           /* S = Selected, N = Not Selected
           N
           S S N
           */
        // Assuming that the user wishes to remove all selected, the right-most selected actions region is part of the link of
        // the left-most selected actions region. Hence, I can expect this actions region to be included twice in deletedFlowingRegions.
        deletedFlowingRegions = flowingRegions.stream()
                .map(getFlowGrid()::getPostLink)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        List<Integer> rows = deletedFlowingRegions.stream()
                .map(FlowGrid::getRowIndex)
                .distinct()
                .collect(Collectors.toList());

        updateMap = getUpdateMap(rows);
    }


    @Override
    public void execute() {
        getFlowGrid().getChildren().removeAll(deletedFlowingRegions);

        // post-removal, visibly, a actions region previously on row 2 will be "seen" on row 1.
        // Yet, its row is still 2 within memory. Below corrects this issue.

        updateMap.forEach((flowingRegion, integers) -> {
            FlowGrid.setRowIndex(flowingRegion, integers.get(FINAL_ROW_POSITION));
        });
    }

    @Override
    public void unexecute() {
        getFlowGrid().getChildren().addAll(deletedFlowingRegions);

        updateMap.forEach((flowingRegion, integers) -> {
            FlowGrid.setRowIndex(flowingRegion, integers.get(PREVIOUS_ROW_POSITION));
        });
    }

    @Override
    public String getName() {
        return "Delete " + deletedFlowingRegions.size() + " regions";
    }
}