package me.theeninja.pfflowing.flowing;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import me.theeninja.pfflowing.configuration.Configuration;
import me.theeninja.pfflowing.gui.ContentContainer;
import me.theeninja.pfflowing.gui.FlowingColumn;

public class FlowingRegion extends Label implements Identifiable {
    private static int currentID = 0;
    private int id;

    private final String representation;

    public FlowingRegion(String representation) {
        super(representation);
        this.representation = representation;
        this.id = currentID++;
        this.setFont(Configuration.FONT);
    }

    @Override
    public int getID() {
        return id;
    }

    public String getRepresentation() {
        return representation;
    }

    public FlowingColumn getFlowingColumn() {
        return (FlowingColumn) getContainer().getParent();
    }

    public ContentContainer getContainer() {
        Parent parent = getParent();
        if (parent instanceof Group)
            return (ContentContainer) parent.getParent();
        else
            return (ContentContainer) parent;
    }
}
