package me.theeninja.pfflowing.gui;

import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.flowing.*;
import me.theeninja.pfflowing.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

public class ContentContainer extends VBox {
    public List<OffensiveFlowingRegion> getRefContent() {
        return Utils.getOfType(this.getChildren(), OffensiveFlowingRegion.class);
    }

    public List<DefensiveFlowingRegion> getBaseContent() {
        return Utils.getOfType(this.getChildren(), DefensiveFlowingRegion.class);
    }

    public List<ExtensionFlowingRegion> getExtensionContent() {
        return Utils.getOfType(this.getChildren(), ExtensionFlowingRegion.class);
    }

    public List<FlowingRegion> getContent() {
        return Utils.getOfType(this.getChildren(), FlowingRegion.class);
    }

    public FlowingColumn getFlowingColumn() {
        return (FlowingColumn) getParent();
    }
}
