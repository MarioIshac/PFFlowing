package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.speech.Side;

public class ExtensionFlowingRegion extends FlowingRegion implements Extension {
    private final Side initiator;
    private final FlowingRegion base;

    public ExtensionFlowingRegion(Side initiator, FlowingRegion base) {
        super("Ext.");
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
