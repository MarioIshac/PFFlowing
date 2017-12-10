package me.theeninja.pfflowing;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

public interface SingleViewController<ViewType extends Node> {
    ViewType getCorrelatingView();
}
