package me.theeninja.pfflowing.tournament;

import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.speech.Side;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Round {
    public static final String NAME = "name";
    public static final String SIDE = "side";
    public static final String AFF_FLOWING_GRID = "affFlowingGrid";
    public static final String NEG_FLOWING_GRID = "negFlowingGrid";

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

        displayedSideProperty().addListener(this::onDisplayedSideChanged);

        SIDE_CONTROLLER_MAP = ImmutableMap.of(
                Side.AFFIRMATIVE, getAffController(),
                Side.NEGATION, getNegController()
        );
    }

    public Round(FlowGrid affFlowGrid, FlowGrid negFlowGrid, Side side) {
        this(side);

        getAffController().flowGrid.getChildren().setAll(affFlowGrid.getChildren());
        getNegController().flowGrid.getChildren().setAll(negFlowGrid.getChildren());
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

    private final Map<Side, FlowDisplayController> SIDE_CONTROLLER_MAP;

    private void onDisplayedSideChanged(ObservableValue<? extends Side> observable, Side oldValue, Side newValue) {
        setSelectedController(SIDE_CONTROLLER_MAP.get(newValue));
    }

    public List<FlowDisplayController> getSideControllers() {
        return List.of(getAffController(), getNegController());
    }
}