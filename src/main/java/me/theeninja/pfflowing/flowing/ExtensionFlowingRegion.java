package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.speech.Side;

public class ExtensionFlowingRegion extends FlowingRegion implements Extension {
    private final FlowingRegion base;

    public ExtensionFlowingRegion(FlowingRegion base) {
        super("Ext.");
        setStyle("-fx-font-weight: bold");
        this.base = base;
    }

    @Override
    public FlowingRegion getBase() {
        return base;
    }
}
