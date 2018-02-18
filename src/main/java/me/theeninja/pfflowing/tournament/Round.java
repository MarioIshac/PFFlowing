package me.theeninja.pfflowing.tournament;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import me.theeninja.pfflowing.gui.AffirmativeFlowingGridController;
import me.theeninja.pfflowing.gui.FlowingGrid;
import me.theeninja.pfflowing.gui.FlowingGridController;
import me.theeninja.pfflowing.gui.NegationFlowingGridController;
import me.theeninja.pfflowing.utils.Utils;

public class Round {
    private String name;
    private final AffirmativeFlowingGridController affController;
    private final NegationFlowingGridController negController;
    private final ObjectProperty<FlowingGridController> selectedController = new SimpleObjectProperty<>();

    public Round(FlowingGrid affFlowingGrid, FlowingGrid negFlowingGrid, String name) {
        affController = Utils.getCorrelatingController("/aff_flowing_pane.fxml");
        negController = Utils.getCorrelatingController("/neg_flowing_pane.fxml");
        affController.getCorrelatingView().getChildren().addAll(affFlowingGrid.getChildren());
        affController.getCorrelatingView().getChildren().addAll(negFlowingGrid.getChildren());
        setName(name);
    }

    public AffirmativeFlowingGridController getAffController() {
        return affController;
    }

    public NegationFlowingGridController getNegController() {
        return negController;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FlowingGridController getSelectedController() {
        return selectedController.get();
    }

    public ObjectProperty<FlowingGridController> selectedControllerProperty() {
        return selectedController;
    }

    public void setSelectedController(FlowingGridController flowingGridController) {
        selectedController.set(flowingGridController);
    }
}
