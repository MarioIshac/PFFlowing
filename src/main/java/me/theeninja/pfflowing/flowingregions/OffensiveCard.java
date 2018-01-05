package me.theeninja.pfflowing.flowingregions;

import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.card.CardContent;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.OffensiveFlowingRegion;

import java.util.Calendar;

public class OffensiveCard extends OffensiveFlowingRegion implements Card {
    private final Author author;
    private final String source;
    private final Calendar date;
    private final CardContent cardContnet;
    private final Side initiator;
    private final Side targetSide;
    private final FlowingRegion targetFlowingRegion;

    public OffensiveCard(Author author, String source, Calendar date, CardContent cardContnet, Side initiator, Side targetSide, FlowingRegion targetFlowingRegion) {
        super(Card.generateRepresentation(author, date), initiator, targetSide, targetFlowingRegion);
        this.author = author;
        this.source = source;
        this.date = date;
        this.cardContnet = cardContnet;
        this.initiator = initiator;
        this.targetSide = targetSide;
        this.targetFlowingRegion = targetFlowingRegion;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public CardContent getCardContent() {
        return cardContnet;
    }

    @Override
    public Author getAuthor() {
        return author;
    }

    @Override
    public Side getInitiator() {
        return initiator;
    }

    @Override
    public Calendar getDate() {
        return date;
    }

    @Override
    public Side getTargetSide() {
        return targetSide;
    }

    @Override
    public FlowingRegion getTargetRegion() {
        return targetFlowingRegion;
    }
}
