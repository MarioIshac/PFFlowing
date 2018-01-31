package me.theeninja.pfflowing.flowingregions;

import javafx.scene.text.Text;
import me.theeninja.pfflowing.Duplicable;

public class FlowingText extends Text implements Duplicable<FlowingText> {
    public FlowingText(String representation) {
        super(representation);
    }

    @Override
    public FlowingText duplicate() {
        FlowingText flowingText = new FlowingText(getText());
        flowingText.setStyle(flowingText.getStyle());
        return flowingText;
    }
}
