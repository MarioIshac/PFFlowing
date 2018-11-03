package me.theeninja.pfflowing.actions;

import javafx.scene.Node;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.Speech;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javafx.scene.layout.GridPane.getRowIndex;

public abstract class FlowAction extends Action<FlowDisplayController> {
    protected FlowAction(FlowDisplayController node) {
        super(node);
    }

    /**
     * Generates a map that represents what changes would occur to the actions grid upon removing
     * a list of rows from {@code flowGrid}. However, we must take into account a row that may
     * be inserted in the case of actions such as merge, which collapses rows (listed as {@code removedRows}).
     *
     * Note that this method should be called before {@code removedRows} are removed.
     *
     * @param removedRows The rows that will be removed from the {@code flowGrid}.
     * @return a map consisting of each affected actions region as a key and a list of two integers as each value,
     *         containing two elements. The first element represents the original row of the actions region. The
     *         second element represents the new row of the actions region, dependent on {@code removedRows}.
     */
    protected Map<FlowingRegion, List<Integer>> getUpdateMap(List<Integer> removedRows) {
        Map<FlowingRegion, List<Integer>> map = new HashMap<>();

        for (Speech speech : getScale().getSpeechList().getSpeeches()) {
            List<Node> speechChildren = getFlowGrid().getColumnChildren(speech.getColumn());
            List<FlowingRegion> flowingRegions = Utils.getOfType(
                    speechChildren,
                    FlowingRegion.class
            );

            for (FlowingRegion flowingRegion : flowingRegions) {

                final int previousRow = getRowIndex(flowingRegion);

                // This will be a removed row anyways, no need to tamper with
                if (removedRows.contains(previousRow))
                    continue;

                /*
                Imagine a tower of ham slices here. When you take a ham slice off from the bottom, all the other ham slices
                will fall down by one index within the ham tower. removedRowsUnder represents the number of rows
                that were collapsed under defensiveFlowingRegion
                 */
                int removedRowsUnder = (int) removedRows.stream().filter(removedRow -> previousRow > removedRow).count();

                int finalRow = previousRow - removedRowsUnder;

                map.put(flowingRegion, List.of(previousRow, finalRow));
            }
        }

        return map;
    }

    protected FlowGrid getFlowGrid() {
        return getScale().flowGrid;
    }
}
