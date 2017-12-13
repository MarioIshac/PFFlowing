package me.theeninja.pfflowing.flowing;

import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import me.theeninja.pfflowing.Configuration;

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
}
