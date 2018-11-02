package me.theeninja.pfflowing.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyEvent;
import me.theeninja.pfflowing.FlowApp;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowingregions.Blocks;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.utils.Utils;

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
        String blocksName = blocks.getName();
        Card blockHeader = new Card(blocksName, null);

        TreeItem<Card> dummyBlockHeader = new TreeItem<>(blockHeader);

        // Set existing tree item children to associated block cards
        setTreeItemChildren(dummyBlockHeader, blocks.getCards());

        // If blocks is edited, reset the header children to the cards
        blocks.getCards().addListener(Utils.generateListChangeListener(
            () -> setTreeItemChildren(dummyBlockHeader, blocks.getCards())
        ));

        root.getChildren().add(dummyBlockHeader);
    }

    private void setTreeItemChildren(TreeItem<Card> dummyBlockHeader, List<Card> cards) {
        List<TreeItem<Card>> treeItems = cards.stream().map(TreeItem::new).collect(Collectors.toList());
        dummyBlockHeader.getChildren().setAll(treeItems);
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
        cardSelectorTreeView.setCellFactory(this::newCardTreeCell);
        root.setExpanded(true);
        getCorrelatingView().setFocusTraversable(false);
    }

    public FlowApp getFlowApp() {
        return flowApp;
    }

    public List<Card> getCards() {
        return root.getChildren().stream()
                // Filters out root and the dummy block headers
                .map(TreeItem::getChildren)
                .flatMap(List::stream)
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

    private TreeCell<Card> newCardTreeCell(TreeView<Card> cardTreeView) {
        CardTreeCell cardTreeCell = new CardTreeCell();
        cardTreeCell.prefWidthProperty().bind(cardSelectorTreeView.prefWidthProperty());

        return cardTreeCell;
    }
}
