package me.theeninja.pfflowing.flowingregions;

import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.card.CardContent;
import me.theeninja.pfflowing.flowing.DefensiveFlowingRegion;

import java.util.Calendar;

public class Card {
    private String author;
    private String source;
    private String date;
    private CardContent cardContnet;

    public Card() {

    }

    public Card(String author, String source, String date, CardContent cardContnet) {
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

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public static String generateRepresentation(String author, String date) {
        return author + (date != null ? " " + date : " ");
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCardContent(CardContent cardContnet) {
        this.cardContnet = cardContnet;
    }
}
