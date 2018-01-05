package me.theeninja.pfflowing.flowingregions;

import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.card.CardContent;
import me.theeninja.pfflowing.flowing.DefensiveFlowingRegion;

import java.util.Calendar;

public class DefensiveCard extends DefensiveFlowingRegion implements Card {
    private final Author author;
    private final String source;
    private final Calendar date;
    private final CardContent cardContnet;
    private final Side initiator;

    public DefensiveCard(Author author, String source, Calendar date, CardContent cardContnet, Side initiator) {
        super(Card.generateRepresentation(author, date));
        this.author = author;
        this.source = source;
        this.date = date;
        this.cardContnet = cardContnet;
        this.initiator = initiator;
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
}
