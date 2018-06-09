package me.theeninja.pfflowing.gui;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.*;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.speech.Side;

public class CardTreeCell extends TreeCell<Card> {
    CardTreeCell() {
        treeItemProperty().addListener(this::onParentTreeItemChanged);
    }

    private void onDragDone(DragEvent dragEvent) {
        System.out.println("Drag finished on node " + dragEvent.getGestureTarget());
    }

    private void addDragSupport() {
        addEventHandler(MouseEvent.DRAG_DETECTED, this::onDragDetected);
        addEventHandler(DragEvent.DRAG_DONE, this::onDragDone);
    }

    private void removeDragSupport() {
        removeEventHandler(MouseEvent.DRAG_DETECTED, this::onDragDetected);
        removeEventHandler(DragEvent.DRAG_DONE, this::onDragDone);
    }

    @Override
    public void updateItem(Card item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty)
            setText(item.getRepresentation());
        else
            setText("");
    }

    private void onParentTreeItemChanged(ObservableValue<? extends TreeItem<Card>> observable, TreeItem<Card> oldValue, TreeItem<Card> newValue) {
        updateTextFill(newValue);
        updateDragSupport(newValue);
    }

    private void updateDragSupport(TreeItem<Card> newValue) {
        if (newValue == null) {
            removeDragSupport();
            return;
        }

        Card card = newValue.getValue();

        // Indicates that this card is a dummy card only used for headers
        if (card.getRepresentation() == null)
            removeDragSupport();
        else
            addDragSupport();
    }

    private void updateTextFill(TreeItem<Card> newValue) {
        if (newValue == null) {
            textFillProperty().unbind();
            return;
        }

        Card card = newValue.getValue();

        // Indicates that this is a dummy cell
        if (card.getSide() == null || card.getRepresentation() == null)
            return;

        Side side = card.getSide();

        textFillProperty().bind(side == Side.AFFIRMATIVE ?
                EFlow.getInstance().getConfiguration().getAffColor().valueProperty() :
                EFlow.getInstance().getConfiguration().getNegColor().valueProperty());
    }

    private void onDragDetected(MouseEvent mouseEvent) {
        System.out.println("Drag detected on card " + getTreeItem().getValue());

        Dragboard dragboard = startDragAndDrop(TransferMode.ANY);
        ClipboardContent content = new ClipboardContent();

        Card card = getTreeItem().getValue();
        String cardName = card.getRepresentation();
        content.putString(cardName);

        dragboard.setContent(content);
    }
}
