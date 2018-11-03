package me.theeninja.pfflowing.actions;

import me.theeninja.pfflowing.actions.Action;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.gui.FlowDisplayController;

public class Edit extends Action<FlowingRegion> {
    private final String newText;
    private final String oldText;

    public Edit(FlowingRegion flowingRegion, String newText) {
        super(flowingRegion);
        this.newText = newText;
        this.oldText = flowingRegion.getFullText();
    }


    @Override
    public void execute() {
        getScale().setFullText(getNewText());
    }

    @Override
    public void unexecute() {
        getScale().setFullText(getOldText());
    }

    @Override
    public String getName() {
        return "Edit";
    }

    public String getNewText() {
        return newText;
    }

    public String getOldText() {
        return oldText;
    }
}
