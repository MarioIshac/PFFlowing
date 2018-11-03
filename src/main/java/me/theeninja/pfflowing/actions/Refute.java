package me.theeninja.pfflowing.actions;

import me.theeninja.pfflowing.actions.Action;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionType;
import me.theeninja.pfflowing.flowing.Speech;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.gui.SpeechList;
import me.theeninja.pfflowing.utils.Utils;

/**
 * Refutes all selected nodes in a position relative to the last selected node. This is done by:
 * 1) constructing a actions region writer that, when submitted, yields an offensive actions region
 * 2) constructing a visual link between the newly created offensive actions region and the selected nodes
 */
public class Refute extends FlowAction {

    private final FlowingRegion baseFlowingRegion;
    private FlowingRegion refFlowingRegion;

    public Refute(FlowDisplayController flowDisplayController, FlowingRegion baseFlowingRegion, String text) {
        super(flowDisplayController);

        this.baseFlowingRegion = baseFlowingRegion;

        SpeechList speechList = getScale().getSpeechList();

        Speech baseSpeech = speechList.getSpeech(baseFlowingRegion);
        // Utils.getRelativeElement(...) will wrap around, yet you cannot refute AT-Neg4 or AT-Aff4 CardContent
        if (Utils.isLastElement(speechList.getSpeeches(), baseSpeech))
            return;

        this.refFlowingRegion = new FlowingRegion(text, FlowingRegionType.REFUTATION);

        int baseRow = FlowGrid.getRowIndex(baseFlowingRegion);
        int baseColumn = FlowGrid.getColumnIndex(baseFlowingRegion);

        int refColumn = baseColumn + FlowGrid.REF_COL_OFFSET;

        FlowGrid.setRowIndex(refFlowingRegion, baseRow);
        FlowGrid.setColumnIndex(refFlowingRegion, refColumn);
    }

    @Override
    public void execute() {
        getFlowGrid().getChildren().add(refFlowingRegion);
    }

    @Override
    public void unexecute() {
        getFlowGrid().getChildren().remove(refFlowingRegion);
    }

    @Override
    public String getName() {
        return "Refute \"" + getActionIdentifier(getBaseFlowingRegion()) + "\"";
    }

    public FlowingRegion getBaseFlowingRegion() {
        return baseFlowingRegion;
    }
}