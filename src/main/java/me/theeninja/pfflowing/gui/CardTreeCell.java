package me.theeninja.pfflowing.gui;

import javafx.scene.control.TreeCell;
import javafx.scene.input.*;
import me.theeninja.pfflowing.flowingregions.Card;

public class CardTreeCell extends TreeCell<Card> {
    CardTreeCell() {
        addDragSupport();
    }

    private void addDragSupport() {
        addEventHandler(MouseEvent.DRAG_DETECTED, mouseEvent -> {
            System.out.println("Drag detected on card " + getTreeItem().getValue());

            Dragboard dragboard = startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();

            Card card = getTreeItem().getValue();
            String cardName = card.getRepresentation();
            content.putString(cardName);

            dragboard.setContent(content);
        });

        addEventHandler(DragEvent.DRAG_DONE, dragEvent -> {
            System.out.println("Drag finished on node " + dragEvent.getGestureTarget());
        });
    }

    @Override
    public void updateItem(Card item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty)
            setText(item.getRepresentation());
        else
            setText("");
    }
}
