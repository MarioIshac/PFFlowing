package me.theeninja.pfflowing.actions;

import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionType;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.gui.MergeException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javafx.scene.layout.GridPane.getRowIndex;

/**
 * Merging action. The process of merging involves collecting all the contents in the rows of all the actions regions
 * into the row belonging to the top-most (determined by lowest row index) actions region.
 * @author TheeNinja
 */
public class Merge extends FlowAction {

    private final List<FlowingRegion> addedRegions = new ArrayList<>();
    private final List<FlowingRegion> removedRegions;

    private final Map<FlowingRegion, List<Integer>> updateMap;

    private final int keptRow;

    /**
     * Constructs a merge action given {@code flowingRegions}
     *
     * @param flowingRegions regions to select
     * @throws MergeException
     */
    public Merge(FlowDisplayController flowDisplayController, List<FlowingRegion> flowingRegions) throws MergeException{
        super(flowDisplayController);
        // Represents the top most row of the actions regions subject to merging
        keptRow = flowingRegions.stream().map(FlowGrid::getRowIndex).reduce(Integer::min).get();

        removedRegions = flowingRegions.stream()
                .map(getFlowGrid()::getWholeLink)
                .sorted(Comparator.comparingInt(list -> getRowIndex(list.get(0))))
                .distinct()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        Map<Integer, List<FlowingRegion>> columnRegionsMap = removedRegions.stream()
                .collect(Collectors.groupingBy(FlowGrid::getColumnIndex));

        for (Map.Entry<Integer, List<FlowingRegion>> entry : columnRegionsMap.entrySet()) {
            int column = entry.getKey();
            List<FlowingRegion> flowingRegionList = entry.getValue();

            FlowingRegion toAdd = condense(flowingRegionList);

            FlowGrid.setConstraints(toAdd, column, getKeptRow());
            addedRegions.add(toAdd);
        }

        List<Integer> rows = removedRegions.stream()
                .map(FlowGrid::getRowIndex)
                .distinct()
                .collect(Collectors.toList());

        // We are reinserting kept row anyway, so remove it from the rows to be removed.
        rows.remove(getKeptRow());

        this.updateMap = getUpdateMap(rows);
    }

    private FlowingRegion condense(List<FlowingRegion> flowingRegions) throws MergeException {
        String condensedText = flowingRegions.stream()
                .map(FlowingRegion::getFullText)
                .collect(Collectors.joining("-"));

        List<Card> associatedCards = flowingRegions.stream()
                .map(FlowingRegion::getAssociatedCards)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        FlowingRegion flowingRegion = null;

        if (flowingRegions.stream().allMatch(FlowingRegion::isProactive))
            flowingRegion = new FlowingRegion(condensedText, FlowingRegionType.PROACTIVE);

        if (flowingRegions.stream().allMatch(FlowingRegion::isOffensive)) {
            flowingRegions.sort(Comparator.comparingInt(FlowGrid::getRowIndex));

            flowingRegion = new FlowingRegion(
                    condensedText,
                    FlowingRegionType.REFUTATION
            );
        }

        if (flowingRegions.stream().allMatch(FlowingRegion::isExtension)) {
            flowingRegions.sort(Comparator.comparingInt(FlowGrid::getRowIndex));

            FlowingRegion topFlowingRegion = flowingRegions.get(0);

            flowingRegion = new FlowingRegion(
                    "Extension",
                    FlowingRegionType.EXTENSION
            );

        }

        // If this passes, indicates that the list of actions regions were different types
        if (flowingRegion == null)
            throw new MergeException("Multiple types of actions regions within single speech");

        return flowingRegion;
    }

    @Override
    public void execute() {
        getFlowGrid().getChildren().removeAll(removedRegions);
        getFlowGrid().getChildren().addAll(addedRegions);

        updateMap.forEach((flowingRegion, integers) -> {
            FlowGrid.setRowIndex(flowingRegion, integers.get(FINAL_ROW_POSITION));
        });
    }

    @Override
    public void unexecute() {
        getFlowGrid().getChildren().removeAll(addedRegions);
        getFlowGrid().getChildren().addAll(removedRegions);

        updateMap.forEach((flowingRegion, integers) -> {
            FlowGrid.setRowIndex(flowingRegion, integers.get(PREVIOUS_ROW_POSITION));
        });
    }

    @Override
    public String getName() {
        return "Merge " + removedRegions.size() + " regions";
    }

    public int getKeptRow() {
        return keptRow;
    }
}