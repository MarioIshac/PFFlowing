package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.flowingregions.FlowingText;
import me.theeninja.pfflowing.gui.FlowingGridController;
import me.theeninja.pfflowing.speech.Side;

import java.util.List;

public class ExtensionFlowingRegion extends FlowingRegion implements Extension {
    private final Side initiator;
    private final FlowingRegion base;

    public ExtensionFlowingRegion(List<FlowingText> flowingTextList, Side initiator, FlowingRegion base, FlowingGridController instance) {
        super(flowingTextList, instance);
        this.initiator = initiator;
        this.base = base;
    }

    @Override
    public Side getInitiator() {
        return initiator;
    }

    @Override
    public FlowingRegion getBase() {
        return base;
    }
}
