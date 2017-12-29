package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.speech.Side;

public interface Offensive {
    Side getInitiator();
    Side getTargetSide();
    FlowingRegion getTargetRegion();
}
