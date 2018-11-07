package me.theeninja.pfflowing.actions;

import me.theeninja.pfflowing.actions.Action;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.Speech;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.gui.FlowGrid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProactiveWrite extends FlowAction {
    private final FlowingRegion flowingRegion;

    private final Map<FlowingRegion, List<Integer>> updateMap = new HashMap<>();

    public ProactiveWrite(FlowDisplayController flowDisplayController, Speech speech, FlowingRegion flowingRegion) {
        super(flowDisplayController);

        this.flowingRegion = flowingRegion;

        FlowGrid.setColumnIndex(getFlowingRegion(), speech.getColumn());
        FlowGrid.setRowIndex(getFlowingRegion(), speech.getAvailableRow());

            /* Imagine a scenario like this, where R = defensive actions region;

                 Column Index
               0 1 2 3 4 5 6 7
               R
               R
               R
                 R
                   R

               We would expect a user would add another actions region to a column index greater or equal to 2. However,
               we must account for the fact that they may want to add another actions region to column 0 (perhaps
               they forgot to flow something of the construction speech. In general, they may want to add something to
               a column that does not have the most recently added defensive actions regions.

               To support this case, the following for-loop only runs if there are defensive actions regions located
               in the speeches after the speech that the user is adding a defensive actions region to. We increment
               the row indexes of all those defensive actions regions by 1.
            */
        for (int column = speech.getColumn() + 1; column < Speech.SPEECH_SIZE; column++) {
            List<FlowingRegion> affectedRegions =  getFlowGrid().getColumnChildren(column).stream()
                .filter(FlowingRegion.class::isInstance)
                .map(FlowingRegion.class::cast)
                .filter(FlowingRegion::isProactive)
                .collect(Collectors.toList());


            for (FlowingRegion affectedRegion : affectedRegions) {
                int row = FlowGrid.getRowIndex(affectedRegion);
                updateMap.put(affectedRegion, List.of(row, row + 1));
            }
        }
    }

    @Override
    public void execute() {
        getFlowGrid().getChildren().add(getFlowingRegion());

        updateMap.forEach((key, value) -> {
            FlowGrid.setRowIndex(key, value.get(FINAL_ROW_POSITION));
        });
    }

    @Override
    public void unexecute() {
        getFlowGrid().getChildren().remove(getFlowingRegion());

        updateMap.forEach((key, value) -> {
            FlowGrid.setRowIndex(key, value.get(PREVIOUS_ROW_POSITION));
        });
    }

    @Override
    public String getName() {
        return "Write \"" + getActionIdentifier(getFlowingRegion()) + "\"";
    }

    public FlowingRegion getFlowingRegion() {
        return flowingRegion;
    }
}