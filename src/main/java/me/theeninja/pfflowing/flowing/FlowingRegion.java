package me.theeninja.pfflowing.flowing;

import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.awt.*;

public class FlowingRegion extends Text implements Identifiable {
    private static int currentID = 0;
    private int id;

    private final String representation;

    public FlowingRegion(String representation) {
        super(representation);
        this.representation = representation;
        this.id = currentID++;
    }

    @Override
    public int getID() {
        return id;
    }

    public String getRepresentation() {
        return representation;
    }
}
