package me.theeninja.pfflowing.flowingregions;

public class Card {
    private String card;
    private String htmlCardContent;

    public Card(String card, String htmlCardContent) {
        this.card = card;
        this.htmlCardContent = htmlCardContent;
    }

    public String getHtmlCardContent() {
        return htmlCardContent;
    }

    public void setHtmlCardContent(String htmlCardContent) {
        this.htmlCardContent = htmlCardContent;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }
}
