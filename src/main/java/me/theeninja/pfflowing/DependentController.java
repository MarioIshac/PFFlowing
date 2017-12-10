package me.theeninja.pfflowing;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.List;

public interface DependentController<ViewType extends Node, ViewParameterType extends Node> extends SingleViewController<ViewType> {
    void setDisplay(List<ViewParameterType> viewParameter);
    void clearDisplay();
    void addToDisplay(ViewParameterType viewParameter);
    void addAllToDisplay(List<ViewParameterType> viewParameter);
    void removeFromDisplay(ViewParameterType viewParameter);
    void removeAllFromDisplay(List<ViewParameterType> viewParameter);
}
