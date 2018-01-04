package me.theeninja.pfflowing.flowingregions.reasoning;

import me.theeninja.pfflowing.flowing.ExtensionFlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowingregions.Reasoning;
import me.theeninja.pfflowing.speech.Side;

public class ExtensionReasoning extends ExtensionFlowingRegion implements Reasoning {
    public ExtensionReasoning(String representation, Side initiator, FlowingRegion base) {
        super(representation, initiator, base);
    }
}
