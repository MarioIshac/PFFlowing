package me.theeninja.pfflowing.flowingregions;

import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.card.CardContent;

import java.util.Calendar;

public interface Card {
    String getSource();
    CardContent getCardContent();
    Author getAuthor();
    Side getInitiator();
    Calendar getDate();

    default String getRepresentation() {
        return Card.generateRepresentation(getAuthor(), getDate());
    }

    static OffensiveCard toOffensiveCard(DefensiveCard dc, Side targetSide, FlowingRegion targetFlowingRegion) {
        return new OffensiveCard(dc.getAuthor(), dc.getSource(), dc.getDate(), dc.getCardContent(), dc.getInitiator(), targetSide, targetFlowingRegion);
    }

    static String generateRepresentation(Author author, Calendar date) {
        return author.getLastName() + " " + date.get(Calendar.YEAR) % 100;
    }
}
