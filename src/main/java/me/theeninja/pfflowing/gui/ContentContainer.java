package me.theeninja.pfflowing.gui;

import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.flowing.*;

import java.util.List;
import java.util.stream.Collectors;

public class ContentContainer extends VBox {
    public List<OffensiveFlowingRegion> getRefContent() {
        return getChildren().stream()
                .map(OffensiveFlowingRegion.class::cast)
                .filter(Offensive.class::isInstance)
                .collect(Collectors.toList());
    }

    public List<DefensiveFlowingRegion> getBaseContent() {
        return getChildren().stream()
                .map(DefensiveFlowingRegion.class::cast)
                .filter(Defensive.class::isInstance)
                .collect(Collectors.toList());
    }

    public FlowingColumn getFlowingColumn() {
        return (FlowingColumn) getParent();
    }
}
