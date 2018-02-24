package me.theeninja.pfflowing.tournament;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.speech.Side;

import java.nio.file.Path;

public class Round {
    private ObjectProperty<Path> path = new SimpleObjectProperty<>();
    private StringProperty name = new SimpleStringProperty();
    private final Side side;
    private ObjectProperty<Side> displayedSide = new SimpleObjectProperty<>();
    private final FlowDisplayController affController;
    private final FlowDisplayController negController;
    private final ObjectProperty<FlowDisplayController> selectedController = new SimpleObjectProperty<>();



    public Round(Side side) {
        affController = FlowDisplayController.newController(Side.AFFIRMATIVE);
        negController = FlowDisplayController.newController(Side.NEGATION);

        this.side = side;

        displayedSideProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == Side.AFFIRMATIVE) {
                setSelectedController(getAffController());
            } else {
                setSelectedController(getNegController());
            }
        }));
    }

    public Round(FlowGrid affFlowGrid, FlowGrid negFlowGrid, Side side) {
        this(side);
        getAffController().flowGrid.getChildren().addAll(affFlowGrid.getChildren());
        getNegController().flowGrid.getChildren().addAll(negFlowGrid.getChildren());
    }

    public FlowDisplayController getAffController() {
        return affController;
    }

    public FlowDisplayController getNegController() {
        return negController;
    }

    public FlowDisplayController getSelectedController() {
        return selectedController.get();
    }

    public ObjectProperty<FlowDisplayController> selectedControllerProperty() {
        return selectedController;
    }

    private void setSelectedController(FlowDisplayController flowDisplayController) {
        selectedController.set(flowDisplayController);
    }

    public Side getSide() {
        return side;
    }

    public Side getDisplayedSide() {
        return displayedSide.get();
    }

    public ObjectProperty<Side> displayedSideProperty() {
        return displayedSide;
    }

    public void setDisplayedSide(Side displayedSide) {
        this.displayedSide.set(displayedSide);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public Path getPath() {
        return path.get();
    }

    public ObjectProperty<Path> pathProperty() {
        return path;
    }

    public void setPath(Path path) {
        this.path.set(path);
    }
}
