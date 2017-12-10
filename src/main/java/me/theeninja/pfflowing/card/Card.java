package me.theeninja.pfflowing.card;

import me.theeninja.pfflowing.Side;
import me.theeninja.pfflowing.flowing.FlowingRegion;

import java.util.Calendar;
import java.util.List;

public class Card extends FlowingRegion {
    private final Author author;
    private final String source;
    private final Calendar date;
    private final Content cardContent;
    private final Side initiator;

    public Card(Author author, String source, Calendar date, Content cardContent, Side initiator) {
        super(author.getLastName() + " " + date.get(Calendar.YEAR) % 100);
        this.author = author;
        this.source = source;
        this.date = date;
        this.cardContent = cardContent;
        this.initiator = initiator;
    }

    public String getSource() {
        return source;
    }

    public Content getCardContent() {
        return cardContent;
    }

    public Author getAuthor() {
        return author;
    }

    public String getRepresentation() {
        return getAuthor().getLastName() + " " + getDate().get(Calendar.YEAR) % 100;
    }

    public Calendar getDate() {
        return date;
    }

    public DefensiveCard toDefensiveCard() {
        return new DefensiveCard(author, source, date, cardContent, initiator);
    }

    public OffensiveCard toOffensiveCard(Side target, List<FlowingRegion> targetRegion) {
        return new OffensiveCard(author, source, date, cardContent, initiator, target, targetRegion);
    }

    public Side getInitiator() {
        return initiator;
    }

    @Override
    public String toString() {
        return getRepresentation();
    }
}
