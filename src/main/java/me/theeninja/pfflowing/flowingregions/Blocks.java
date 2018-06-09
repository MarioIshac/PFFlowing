package me.theeninja.pfflowing.flowingregions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import me.theeninja.pfflowing.speech.Side;

import java.util.ArrayList;
import java.util.List;

public class Blocks {
    public Blocks(String blocksName, Side blocksSide) {
        this.name = blocksName;
        this.side = blocksSide;
        this.cards = FXCollections.observableArrayList();
    }

    @Expose
    @SerializedName("name")
    private final String name;

    @Expose
    @SerializedName("side")
    private final Side side;

    @Expose
    @SerializedName("cards")
    private final ObservableList<Card> cards;

    public String getName() {
        return name;
    }

    public ObservableList<Card> getCards() {
        return cards;
    }

    public Side getSide() {
        return side;
    }
}
