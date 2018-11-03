package me.theeninja.pfflowing.actions;

import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.gui.FlowDisplayController;

import java.util.List;
import java.util.stream.Collectors;

public class Drop extends FlowAction {
    private final List<FlowingRegion> droppedFlowingRegions;

    public Drop(FlowDisplayController flowDisplayController, List<FlowingRegion> flowingRegions) {
        super(flowDisplayController);

        this.droppedFlowingRegions = flowingRegions
                .stream()
                .map(getFlowGrid()::getPostLink)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

    }

    @Override
    public void execute() {
        for (FlowingRegion flowingRegion : getDroppedFlowingRegions())
            flowingRegion.getStyleClass().add("dropped");
    }

    @Override
    public void unexecute() {
        for (FlowingRegion flowingRegion : getDroppedFlowingRegions())
            flowingRegion.getStyleClass().remove("dropped");
    }

    @Override
    public String getName() {
        return "Drop " + getDroppedFlowingRegions().size() + " regions";
    }

    public List<FlowingRegion> getDroppedFlowingRegions() {
        return this.droppedFlowingRegions;
    }
}