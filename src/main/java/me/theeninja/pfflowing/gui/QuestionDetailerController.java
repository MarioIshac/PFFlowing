package me.theeninja.pfflowing.gui;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowing.FlowingRegion;

import java.net.URL;
import java.util.ResourceBundle;

public class QuestionDetailerController implements Detailer, Initializable, SingleViewController<Text> {
    private final FlowingRegion flowingRegion;

    @FXML
    public Text questionText;

    public QuestionDetailerController(FlowingRegion flowingRegion) {
        this.flowingRegion = flowingRegion;
    }

    @Override
    public boolean hasDetail() {
        return getFlowingRegion().getQuestionText() != null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getCorrelatingView().textProperty().bind(getFlowingRegion().questionTextProperty());
    }

    public FlowingRegion getFlowingRegion() {
        return flowingRegion;
    }

    @Override
    public Text getCorrelatingView() {
        return questionText;
    }
}
