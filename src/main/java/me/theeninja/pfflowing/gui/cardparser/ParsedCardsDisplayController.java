package me.theeninja.pfflowing.gui.cardparser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.utils.Utils;

import java.net.URL;
import java.util.ResourceBundle;

public class ParsedCardsDisplayController implements Initializable, SingleViewController<HBox> {
    public HBox parsedCardsView;
    public Label percentParsedIndicator;

    private final ObservableList<Card> parsedCards = FXCollections.observableArrayList();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getParsedCards().addListener(Utils.generateListChangeListener(this::addDisplayOfParsedCard, eh -> {}));
    }

    public void addDisplayOfParsedCard(Card card) {
        Label parsedCardRepresentation = new Label(Card.generateRepresentation(card.getAuthor(), card.getDate()));
        parsedCardsView.getChildren().add(parsedCardRepresentation);
    }

    public ObservableList<Card> getParsedCards() {
        return parsedCards;
    }

    @Override
    public HBox getCorrelatingView() {
        return parsedCardsView;
    }
}
