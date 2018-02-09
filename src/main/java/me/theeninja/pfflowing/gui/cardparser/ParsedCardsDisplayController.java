package me.theeninja.pfflowing.gui.cardparser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.utils.Utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ParsedCardsDisplayController implements Initializable {
    public HBox parsedCardsView;
    public Label percentParsedIndicator;

    private final ObservableList<Card> parsedCards = FXCollections.observableArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getParsedCards().addListener(Utils.generateListChangeListener(this::addParsedCard, eh -> {}));
    }

    public void addParsedCard(Card card) {
        Label parsedCardRepresentation = new Label(Card.generateRepresentation(card.getAuthor(), card.getDate()));
        parsedCardsView.getChildren().add(parsedCardRepresentation);
    }

    public ObservableList<Card> getParsedCards() {
        return parsedCards;
    }
}
