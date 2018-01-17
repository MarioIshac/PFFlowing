package me.theeninja.pfflowing.gui;

import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import me.theeninja.pfflowing.DependentController;
import me.theeninja.pfflowing.flowingregions.DefensiveCard;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.flowingregions.OffensiveCard;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CardSelectorController implements Initializable, DependentController<TreeView<DefensiveCard>, DefensiveCard> {
    public TreeView<DefensiveCard> cardSelectorTreeView;
    public TreeItem<DefensiveCard> root;

    private Logger logger = Logger.getLogger(CardSelectorController.class.getSimpleName());

    private static CardSelectorController fxmlInstance;

    public static CardSelectorController getFXMLInstance() {
        return fxmlInstance;
    }

    @Override
    public TreeView<DefensiveCard> getCorrelatingView() {
        return cardSelectorTreeView;
    }

    private void addTreeItemCardInfo(TreeItem<DefensiveCard> treeItem) {
        DefensiveCard correlatingCard = treeItem.getValue();
        String authorLabel = "Author" + correlatingCard.getAuthor();
    }

    @Override
    public void setDisplay(List<DefensiveCard> viewParameter) {
        root.getChildren().setAll(viewParameter.stream()
                .map(TreeItem::new)
                .collect(Collectors.toList()));
    }

    @Override
    public void clearDisplay() {
        root.getChildren().clear();
    }

    @Override
    public void addToDisplay(DefensiveCard viewParameter) {
        root.getChildren().add(new TreeItem<>(viewParameter));
    }

    @Override
    public void addAllToDisplay(List<DefensiveCard> viewParameter) {
        root.getChildren().addAll(viewParameter.stream()
                .map(TreeItem::new)
                .collect(Collectors.toList()));
    }

    @Override
    public void removeFromDisplay(DefensiveCard viewParameter) {
        root.getChildren().remove(new TreeItem<>(viewParameter));

    }

    @Override
    public void removeAllFromDisplay(List<DefensiveCard> viewParameter) {
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

    private class CardTreeCell extends TreeCell<DefensiveCard> {
        @Override
        public void updateItem(DefensiveCard item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty)
                setText(item.getRepresentation());
            else
                setText("");
        }
    }

    private EventHandler<KeyEvent> handler = keyEvent -> {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            TreeItem<DefensiveCard> treeItem = cardSelectorTreeView.getSelectionModel().getSelectedItem();
            OffensiveCard offensiveCard = Card.toOffensiveCard(treeItem.getValue(), Side.AFFIRMATIVE, FlowingGridController.getFXMLInstance().getLastSelected());
           // FlowingGridController.getFXMLInstance().addOffensiveFlowingRegion(offensiveCard);
            removeCardSelectionListener();
        }
    };

    public void addCardSelectionListener() {
        cardSelectorTreeView.getSelectionModel().selectFirst();
        cardSelectorTreeView.addEventHandler(KeyEvent.KEY_PRESSED, handler);
    }

    public void removeCardSelectionListener() {
        FlowingGridController.getFXMLInstance().getCorrelatingView().requestFocus();
        cardSelectorTreeView.removeEventHandler(KeyEvent.KEY_PRESSED, handler);
    }
}
