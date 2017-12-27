package me.theeninja.pfflowing.card;

import me.theeninja.pfflowing.Side;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.Offensive;

import java.util.List;

public class OffensiveReasoning extends Reasoning implements Offensive {
    private final Side initiator;
    private final Side target;
    private final List<FlowingRegion> targetRegion;

    public OffensiveReasoning(String representation, Side initiator, Side target, List<FlowingRegion> targetRegion) {
        super(representation);
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
    public List<FlowingRegion> getTargetRegion() {
        return targetRegion;
    }
}
