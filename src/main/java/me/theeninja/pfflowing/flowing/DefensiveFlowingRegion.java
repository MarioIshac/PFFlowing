package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.configuration.GlobalConfiguration;
import me.theeninja.pfflowing.gui.FlowingGridController;
import me.theeninja.pfflowing.gui.LengthLimitType;

public class DefensiveFlowingRegion extends FlowingRegion implements Defensive {
    public DefensiveFlowingRegion(String fullText, FlowingGridController instance, LengthLimitType lengthLimitType, int limit) {
        super(fullText, instance, lengthLimitType, limit);
    }
}
