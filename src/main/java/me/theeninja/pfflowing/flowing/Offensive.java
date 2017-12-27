package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.Side;

import java.util.List;

public interface Offensive {
    Side getInitiator();
    Side getTargetSide();
    FlowingRegion getTargetRegion();
}
