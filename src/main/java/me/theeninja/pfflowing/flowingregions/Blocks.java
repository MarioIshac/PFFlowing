package me.theeninja.pfflowing.flowingregions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.theeninja.pfflowing.speech.Side;

import java.util.List;

public class Blocks {
    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("side")
    private Side side;

    @Expose
    @SerializedName("cards")
    private List<Card> cards;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }
}
