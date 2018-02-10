package me.theeninja.pfflowing.flowingregions;

import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.card.CardContent;
import me.theeninja.pfflowing.flowing.DefensiveFlowingRegion;

import java.util.Calendar;

public class Card {
    private Author author;
    private String source;
    private Calendar date;
    private CardContent cardContnet;

    public Card() {

    }

    public Card(Author author, String source, Calendar date, CardContent cardContnet) {
        this.author = author;
        this.source = source;
        this.date = date;
        this.cardContnet = cardContnet;
    }

    public String getSource() {
        return source;
    }

    public CardContent getCardContent() {
        return cardContnet;
    }

    public Author getAuthor() {
        return author;
    }

    public Calendar getDate() {
        return date;
    }

    public static String generateRepresentation(Author author, Calendar date) {
        return author.getLastName() + (date != null ? " " + date.get(Calendar.YEAR) % 100 : " ");
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setCardContent(CardContent cardContnet) {
        this.cardContnet = cardContnet;
    }
}
