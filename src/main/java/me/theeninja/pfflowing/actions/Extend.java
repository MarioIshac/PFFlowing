package me.theeninja.pfflowing.actions;

import me.theeninja.pfflowing.actions.Action;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionType;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.gui.FlowLink;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Extend extends FlowAction {
    private final List<FlowingRegion> baseFlowingRegions;
    private final List<FlowingRegion> extendFlowingRegions;
    private List<FlowLink> flowLinks = new ArrayList<>();

    public Extend(FlowDisplayController flowDisplayController, List<FlowingRegion> baseFlowingRegions) {
        super(flowDisplayController);
        this.baseFlowingRegions = baseFlowingRegions;
        this.extendFlowingRegions = this.baseFlowingRegions.stream()
                .map(FlowingRegion::duplicate)
                .map(this::newExtensionFromBase)
                .collect(Collectors.toList());
    }

    @Override
    public void execute() {
        getFlowGrid().getChildren().addAll(extendFlowingRegions);
        addFlowLinks();

    }

    @Override
    public void unexecute() {
        getFlowGrid().getChildren().removeAll(this.extendFlowingRegions);
        removeFlowLinks();
    }

    @Override
    public String getName() {
        return "Extend " + baseFlowingRegions.size() + " regions";
    }

    private void addFlowLinks() {
        for (int index = 0; index < extendFlowingRegions.size(); index++) {
            FlowingRegion base = baseFlowingRegions.get(index);
            FlowingRegion extension = extendFlowingRegions.get(index);

            FlowLink flowLink = new FlowLink(base, extension);
            flowLinks.add(flowLink);
        }

        flowLinks.stream()
                .map(FlowLink::getLines)
                .flatMap(List::stream)
                .forEach(getFlowGrid().getChildren()::add);
    }

    private void removeFlowLinks() {
        getFlowGrid().getChildren().removeAll(flowLinks);

    }

    private FlowingRegion newExtensionFromBase(FlowingRegion baseFlowingRegion) {
        FlowingRegion extension = new FlowingRegion("Extend", FlowingRegionType.EXTENSION);

        int baseRowIndex = FlowGrid.getRowIndex(baseFlowingRegion);
        int baseColIndex = FlowGrid.getColumnIndex(baseFlowingRegion);

        int newColIndex = baseColIndex + FlowGrid.EXT_COL_OFFSET;

        FlowGrid.setColumnIndex(extension, newColIndex);
        FlowGrid.setRowIndex(extension, baseRowIndex);

        return extension;
    }
}