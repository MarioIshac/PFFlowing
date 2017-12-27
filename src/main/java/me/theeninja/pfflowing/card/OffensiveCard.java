package me.theeninja.pfflowing.card;

import me.theeninja.pfflowing.Side;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.Offensive;

import java.util.Calendar;
import java.util.List;

public class OffensiveCard extends Card implements Offensive {
    private final Side target;
    private final List<FlowingRegion> targetRegion;

    public OffensiveCard(Author author, String source, Calendar date, Content cardContnet, Side initiator, Side target, List<FlowingRegion> targetRegion) {
        super(author, source, date, cardContnet, initiator);
        this.target = target;
        this.targetRegion = targetRegion;
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
