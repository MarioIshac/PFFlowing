package me.theeninja.pfflowing.flowing;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import me.theeninja.pfflowing.StringSerializable;
import me.theeninja.pfflowing.configuration.Configuration;
import me.theeninja.pfflowing.gui.ContentContainer;
import me.theeninja.pfflowing.gui.FlowingColumn;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FlowingRegion extends Label implements Identifiable, StringSerializable<FlowingRegion> {
    private static int currentID = 0;
    private static Map<Integer, FlowingRegion> idMap;
    private int id;

    private final String representation;

    public static FlowingRegion getFlowingRegion(int id) {
        return idMap.get(id);
    }

    static {
        idMap = new HashMap<>();
    }

    public FlowingRegion(String representation) {
        super(representation);
        this.representation = representation;
        this.id = currentID++;
        this.setFont(Configuration.FONT);

        idMap.put(getID(), this);
    }

    @Override
    public int getID() {
        return id;
    }

    public String getRepresentation() {
        return representation;
    }

    public FlowingColumn getFlowingColumn() {
        return (FlowingColumn) getContentContainer().getParent();
    }

    public ContentContainer getContentContainer() {
        Parent parent = getParent();
        if (parent instanceof Group)
            return (ContentContainer) parent.getParent();
        else
            return (ContentContainer) parent;
    }

    @Override
    public String serialize() {
        return getRepresentation();
    }

    @Override
    public FlowingRegion deserialize(String representation) {
        return new FlowingRegion(representation);
    }
}
