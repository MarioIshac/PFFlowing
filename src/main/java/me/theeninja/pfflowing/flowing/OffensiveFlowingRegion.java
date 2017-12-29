package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.speech.Side;

public class OffensiveFlowingRegion extends FlowingRegion implements Offensive {
    private final Side initiator;
    private final Side targetSide;
    private final FlowingRegion targetFlowingRegino;

    public OffensiveFlowingRegion(String representation, Side initiator, Side targetSide, FlowingRegion targetFlowingRegino) {
        super(representation);
        this.initiator = initiator;
        this.targetSide = targetSide;
        this.targetFlowingRegino = targetFlowingRegino;
    }

    @Override
    public Side getInitiator() {
        return initiator;
    }

    @Override
    public Side getTargetSide() {
        return targetSide;
    }

    @Override
    public FlowingRegion getTargetRegion() {
        return targetFlowingRegino;
    }
}
