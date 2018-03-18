package me.theeninja.pfflowing.gui;

import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.speech.Side;

public class FlowDisplay extends VBox {
    private Side side;

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }
}
