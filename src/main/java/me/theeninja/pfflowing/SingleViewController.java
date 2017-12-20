package me.theeninja.pfflowing;

import javafx.scene.Node;

public interface SingleViewController<ViewType extends Node> {
    ViewType getCorrelatingView();
}
