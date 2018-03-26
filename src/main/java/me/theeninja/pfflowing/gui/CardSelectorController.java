package me.theeninja.pfflowing.gui;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import me.theeninja.pfflowing.DependentController;
import me.theeninja.pfflowing.FlowApp;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowingregions.Blocks;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.speech.Side;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CardSelectorController implements Initializable, SingleViewController<TreeView<Card>> {
    private final FlowApp flowApp;

    CardSelectorController(FlowApp flowApp) {
        this.flowApp = flowApp;
    }

    public void addBlocks(Blocks blocks) {
        blocks.getCards().stream().map(TreeItem::new).forEach(root.getChildren()::add);
    }

    @FXML
    public TreeView<Card> cardSelectorTreeView;

    @FXML
    public TreeItem<Card> root;

    @Override
    public TreeView<Card> getCorrelatingView() {
        return cardSelectorTreeView;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cardSelectorTreeView.setShowRoot(false);
        cardSelectorTreeView.setCellFactory(cardTreeView -> {
            CardTreeCell cardTreeCell = new CardTreeCell();
            cardTreeCell.prefWidthProperty().bind(cardSelectorTreeView.prefWidthProperty());
            return cardTreeCell;
        });
        root.setExpanded(true);
        getCorrelatingView().setFocusTraversable(false);
    }

    public FlowApp getFlowApp() {
        return flowApp;
    }

    public List<Card> getCards() {
        return root.getChildren().stream()
                .map(TreeItem::getValue)
                .collect(Collectors.toList());
    }

    public Card getCard(String cardName) {
        for (Card card : getCards()) {
            if (card.getRepresentation().equals(cardName))
                return card;
        }

        return null;
    }
}
