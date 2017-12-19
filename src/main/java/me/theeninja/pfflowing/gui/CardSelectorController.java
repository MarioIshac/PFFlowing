package me.theeninja.pfflowing.gui;

import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import me.theeninja.pfflowing.DependentController;
import me.theeninja.pfflowing.Side;
import me.theeninja.pfflowing.card.Card;
import me.theeninja.pfflowing.card.OffensiveCard;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CardSelectorController implements Initializable, DependentController<TreeView<Card>, Card> {
    public TreeView<Card> cardSelectorTreeView;
    public TreeItem<Card> root;

    private Logger logger = Logger.getLogger(CardSelectorController.class.getSimpleName());

    private static CardSelectorController fxmlInstance;

    public static CardSelectorController getFXMLInstance() {
        return fxmlInstance;
    }

    @Override
    public TreeView<Card> getCorrelatingView() {
        return cardSelectorTreeView;
    }

    private void addTreeItemCardInfo(TreeItem<Card> treeItem) {
        Card correlatingCard = treeItem.getValue();
        String authorLabel = "Author" + correlatingCard.getAuthor();
    }

    @Override
    public void setDisplay(List<Card> viewParameter) {
        root.getChildren().setAll(viewParameter.stream()
                .map(TreeItem::new)
                .collect(Collectors.toList()));
    }

    @Override
    public void clearDisplay() {
        root.getChildren().clear();
    }

    @Override
    public void addToDisplay(Card viewParameter) {
        root.getChildren().add(new TreeItem<>(viewParameter));
    }

    @Override
    public void addAllToDisplay(List<Card> viewParameter) {
        root.getChildren().addAll(viewParameter.stream()
                .map(TreeItem::new)
                .collect(Collectors.toList()));
    }

    @Override
    public void removeFromDisplay(Card viewParameter) {
        root.getChildren().remove(new TreeItem<>(viewParameter));

    }

    @Override
    public void removeAllFromDisplay(List<Card> viewParameter) {
        root.getChildren().removeAll(viewParameter.stream()
                .map(TreeItem::new)
                .collect(Collectors.toList()));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxmlInstance = this;
        cardSelectorTreeView.setShowRoot(false);
        cardSelectorTreeView.setCellFactory(cardTreeView -> {
            CardTreeCell cardTreeCell = new CardTreeCell();
            cardTreeCell.prefWidthProperty().bind(cardSelectorTreeView.prefWidthProperty());
            return cardTreeCell;
        });
        root.setExpanded(true);
    }

    private class CardTreeCell extends TreeCell<Card> {
        @Override
        public void updateItem(Card item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty)
                setText(item.getRepresentation());
            else
                setText("");
        }
    }

    private EventHandler<KeyEvent> handler = keyEvent -> {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            TreeItem<Card> treeItem = cardSelectorTreeView.getSelectionModel().getSelectedItem();
            OffensiveCard offensiveCard = treeItem.getValue().toOffensiveCard(Side.AFFIRMATIVE, FlowingColumnsController.getFXMLInstance().getSelectedFlowingRegions());
            FlowingColumnsController.getFXMLInstance().getSpeechListManager().getVisibleSelectedSpeech().getBinded().addOffensiveFlowingRegion(offensiveCard);
            removeCardSelectionListener();
        }
    };

    public void addCardSelectionListener() {
        cardSelectorTreeView.getSelectionModel().selectFirst();
        cardSelectorTreeView.addEventHandler(KeyEvent.KEY_PRESSED, handler);
    }

    public void removeCardSelectionListener() {
        FlowingColumnsController.getFXMLInstance().getCorrelatingView().requestFocus();
        cardSelectorTreeView.removeEventHandler(KeyEvent.KEY_PRESSED, handler);
    }
}
