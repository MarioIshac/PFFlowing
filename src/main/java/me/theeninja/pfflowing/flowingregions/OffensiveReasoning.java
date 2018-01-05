package me.theeninja.pfflowing.flowingregions;

import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.OffensiveFlowingRegion;

public class OffensiveReasoning extends OffensiveFlowingRegion implements Reasoning {
    private final Side initiator;
    private final Side target;
    private final FlowingRegion targetRegion;

    public OffensiveReasoning(String representation, Side initiator, Side target, FlowingRegion targetRegion) {
        super(representation, initiator, target, targetRegion);
        this.initiator = initiator;
        this.target = target;
        this.targetRegion = targetRegion;
    }

    @Override
    public Side getInitiator() {
        return initiator;
    }

    @Override
    public Side getTargetSide() {
        return target;
    }

    @Override
    public FlowingRegion getTargetRegion() {
        return targetRegion;
    }
}
