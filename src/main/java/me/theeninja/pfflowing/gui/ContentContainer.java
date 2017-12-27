package me.theeninja.pfflowing.gui;

import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.flowing.Defensive;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.Offensive;

import java.util.List;
import java.util.stream.Collectors;

public class ContentContainer extends VBox {
    public List<FlowingRegion> getRefContent() {
        return getChildren().stream()
                .map(FlowingRegion.class::cast)
                .filter(Offensive.class::isInstance)
                .collect(Collectors.toList());
    }

    public List<FlowingRegion> getBaseContent() {
        return getChildren().stream()
                .map(FlowingRegion.class::cast)
                .filter(Defensive.class::isInstance)
                .collect(Collectors.toList());
    }

    public FlowingColumn getFlowingColumn() {
        return (FlowingColumn) getParent();
    }
}
