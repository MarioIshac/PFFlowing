package me.theeninja.pfflowing.tournament;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.speech.Side;

import java.nio.file.Path;
import java.util.function.Consumer;

public class Round {
    public static final String NAME = "name";
    public static final String SIDE = "side";
    public static final String AFF_FLOWING_GRID = "affFlowingGrid";
    public static final String NEG_FLOWING_GRID = "negFlowingGrid";

    private ObjectProperty<Path> path = new SimpleObjectProperty<>();

    private final String roundName;
    private final Side side;
    private final ObjectProperty<Side> displayedSide = new SimpleObjectProperty<>();
    private final FlowDisplayController affirmativeController;
    private final FlowDisplayController negationController;
    private final ObjectProperty<FlowDisplayController> selectedController = new SimpleObjectProperty<>();

    public Round(String roundName, Side side) {
        this.affirmativeController = FlowDisplayController.newController(Side.AFFIRMATIVE);
        this.negationController = FlowDisplayController.newController(Side.NEGATION);

        this.roundName = roundName;
        this.side = side;

        displayedSideProperty().addListener(this::onDisplayedSideChanged);

        setDisplayedSide(getSide());
    }

    public Round(String roundName, Side side, FlowGrid affFlowGrid, FlowGrid negFlowGrid) {
        this(roundName, side);

        getAffirmativeController().flowGrid.getChildren().setAll(affFlowGrid.getChildren());
        getNegationController().flowGrid.getChildren().setAll(negFlowGrid.getChildren());
    }

    public FlowDisplayController getAffirmativeController() {
        return affirmativeController;
    }

    public FlowDisplayController getNegationController() {
        return negationController;
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

    public String getRoundName() {
        return this.roundName;
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

    private void onDisplayedSideChanged(ObservableValue<? extends Side> observable, Side oldValue, Side newValue) {
        FlowDisplayController flowDisplayController = getController(newValue);

        System.out.println("displayed side changed");
        setSelectedController(flowDisplayController);
    }

    public void applyPerSide(Consumer<FlowDisplayController> consumer) {
        consumer.accept(getAffirmativeController());
        consumer.accept(getNegationController());
    }

    public FlowDisplayController getController(Side side) {
        return side == Side.AFFIRMATIVE ? getAffirmativeController() : getNegationController();
    }
}