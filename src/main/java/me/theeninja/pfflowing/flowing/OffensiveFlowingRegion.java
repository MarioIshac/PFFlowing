package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.speech.Side;

public class OffensiveFlowingRegion extends FlowingRegion implements Offensive {
    private final FlowingRegion targetFlowingRegino;

    public OffensiveFlowingRegion(String fullText, FlowingRegion targetFlowingRegino) {
        super(fullText);
        this.targetFlowingRegino = targetFlowingRegino;
    }

    @Override
    public FlowingRegion getTargetRegion() {
        return targetFlowingRegino;
    }
}
