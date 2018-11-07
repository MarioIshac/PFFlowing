package me.theeninja.pfflowing.actions;

import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionType;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.gui.SplitException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Split extends FlowAction {
    private final FlowingRegion flowingRegion;

    private final Map<FlowingRegion, List<Integer>> updateMap = new HashMap<>();

    private final List<FlowingRegion> addedRegions = new ArrayList<>();
    private final List<FlowingRegion> removedRegions = new ArrayList<>();

    public Split(FlowDisplayController flowDisplayController, FlowingRegion flowingRegion, int split) throws SplitException {
        super(flowDisplayController);

        if (flowingRegion.getFlowingRegionType() != FlowingRegionType.PROACTIVE) {
            throw new SplitException("Cannot split non-defensive actions region");
        }

        this.flowingRegion = flowingRegion;

        getRemovedRegions().add(getFlowingRegion());

        String flowingRegionText = flowingRegion.getFullText();

        String firstPart = flowingRegionText.substring(0, split);
        String secondPart = flowingRegionText.substring(split);

        FlowingRegion firstRegion = new FlowingRegion(firstPart, FlowingRegionType.PROACTIVE);
        FlowingRegion secondRegion = new FlowingRegion(secondPart, FlowingRegionType.PROACTIVE);

        getAddedRegions().add(firstRegion);
        getAddedRegions().add(secondRegion);

        int firstRowIndex = FlowGrid.getRowIndex(flowingRegion);
        int secondRowIndex = firstRowIndex + 1;

        int baseColumn = FlowGrid.getColumnIndex(flowingRegion);

        FlowGrid.setConstraints(firstRegion, baseColumn, firstRowIndex);
        FlowGrid.setConstraints(secondRegion, baseColumn, secondRowIndex);

        IntStream.range(secondRowIndex, getFlowGrid().getRowCount())
            .mapToObj(getFlowGrid()::getRowChildren)
            .filter(FlowingRegion.class::isInstance)
            .map(FlowingRegion.class::cast)
            .forEach(this::handleRegion);

    }

    @Override
    public void execute() {
        getFlowGrid().getChildren().addAll(getAddedRegions());
        getFlowGrid().getChildren().removeAll(getRemovedRegions());

        getUpdateMap().forEach((region, integers) -> {
            int newRowIndex = integers.get(FINAL_ROW_POSITION);
            FlowGrid.setRowIndex(region, newRowIndex);
        });
    }

    @Override
    public void unexecute() {
        getFlowGrid().getChildren().addAll(getAddedRegions());
        getFlowGrid().getChildren().addAll(getRemovedRegions());

        getUpdateMap().forEach((region, integers) -> {
            int oldRowIndex = integers.get(PREVIOUS_ROW_POSITION);
            FlowGrid.setRowIndex(region, oldRowIndex);
        });
    }

    @Override
    public String getName() {
        return "Split";
    }

    public FlowingRegion getFlowingRegion() {
        return flowingRegion;
    }

    public List<FlowingRegion> getAddedRegions() {
        return addedRegions;
    }

    public List<FlowingRegion> getRemovedRegions() {
        return removedRegions;
    }

    public Map<FlowingRegion, List<Integer>> getUpdateMap() {
        return updateMap;
    }

    private void handleRegion(FlowingRegion region) {
        int originalRowIndex = FlowGrid.getRowIndex(region);
        int newRowIndex = originalRowIndex + 1;
        getUpdateMap().put(region, List.of(originalRowIndex, newRowIndex));
    }
}
