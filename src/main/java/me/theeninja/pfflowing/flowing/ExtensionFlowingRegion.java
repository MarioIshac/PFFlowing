package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.gui.FlowingGridController;
import me.theeninja.pfflowing.gui.LengthLimitType;
import me.theeninja.pfflowing.speech.Side;

import java.util.List;

public class ExtensionFlowingRegion extends FlowingRegion implements Extension {
    private final Side initiator;
    private final FlowingRegion base;

    public ExtensionFlowingRegion(LengthLimitType lengthLimitType, int limit, Side initiator, FlowingRegion base, FlowingGridController instance) {
        super("Ext.", instance, lengthLimitType, limit);
        setStyle("-fx-font-weight: bold");
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
