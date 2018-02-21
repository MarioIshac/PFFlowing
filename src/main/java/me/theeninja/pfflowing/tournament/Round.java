package me.theeninja.pfflowing.tournament;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tab;
import me.theeninja.pfflowing.gui.AffirmativeFlowingGridController;
import me.theeninja.pfflowing.gui.FlowingGrid;
import me.theeninja.pfflowing.gui.FlowingGridController;
import me.theeninja.pfflowing.gui.NegationFlowingGridController;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;

import java.util.Map;

public class Round {
    private StringProperty name = new SimpleStringProperty();
    private final Side side;
    private ObjectProperty<Side> displayedSide = new SimpleObjectProperty<>();
    private final AffirmativeFlowingGridController affController;
    private final NegationFlowingGridController negController;
    private final ObjectProperty<FlowingGridController> selectedController = new SimpleObjectProperty<>();

    public Round(Side side) {
        affController = Utils.getCorrelatingController("/aff_flowing_pane.fxml");
        negController = Utils.getCorrelatingController("/neg_flowing_pane.fxml");

        this.side = side;

        displayedSideProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == Side.AFFIRMATIVE) {
                setSelectedController(getAffController());
            } else {
                setSelectedController(getNegController());
            }
        }));
    }

    public Round(FlowingGrid affFlowingGrid, FlowingGrid negFlowingGrid, Side side) {
        this(side);
        getAffController().getCorrelatingView().getChildren().addAll(affFlowingGrid.getChildren());
        getNegController().getCorrelatingView().getChildren().addAll(negFlowingGrid.getChildren());
    }

    public AffirmativeFlowingGridController getAffController() {
        return affController;
    }

    public NegationFlowingGridController getNegController() {
        return negController;
    }

    public FlowingGridController getSelectedController() {
        return selectedController.get();
    }

    public ObjectProperty<FlowingGridController> selectedControllerProperty() {
        return selectedController;
    }

    private void setSelectedController(FlowingGridController flowingGridController) {
        selectedController.set(flowingGridController);
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
}
