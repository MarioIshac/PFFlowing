package me.theeninja.pfflowing;

import javafx.scene.Node;

/**
 * GoogleDriveConnector controller of one, main view.
 *
 * @param <ViewType> The type of node being controlled.
 * @author TheeNinja
 */
public interface SingleViewController<ViewType extends Node> {
    /**
     * Provides the one, main view that is represented by this controller.
     *
     * @return The one, main view correlating to the controller.
     */
    ViewType getCorrelatingView();
}
