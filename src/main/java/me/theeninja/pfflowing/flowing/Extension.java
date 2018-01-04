package me.theeninja.pfflowing.flowing;

import me.theeninja.pfflowing.speech.Side;

public interface Extension {
    Side getInitiator();
    FlowingRegion getBase();
}
