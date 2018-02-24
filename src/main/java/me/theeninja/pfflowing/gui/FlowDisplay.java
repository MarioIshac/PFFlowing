package me.theeninja.pfflowing.gui;

import javafx.scene.layout.HBox;
import me.theeninja.pfflowing.speech.Side;

public class FlowDisplay extends HBox {
    private Side side;

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }
}
