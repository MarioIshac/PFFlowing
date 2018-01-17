package me.theeninja.pfflowing.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import me.theeninja.pfflowing.PFFlowing;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;

import java.net.URL;
import java.util.ResourceBundle;

public class PFFlowingApplicationController implements Initializable, SingleViewController<FlowingPane> {
    @FXML
    public FlowingPane pfFlowingMain;

    private ObjectProperty<Node> currentFocusedNodeProperty = new SimpleObjectProperty<>();


    @Override
    public FlowingPane getCorrelatingView() {
        return pfFlowingMain;
    }

    private static PFFlowingApplicationController fxmlInstance;

    public static PFFlowingApplicationController getFXMLInstance() {
        if (fxmlInstance == null) {
            fxmlInstance = Utils.getCorrelatingController("/pfflowing.fxml");
        }
        return fxmlInstance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentFocusedNodeProperty.addListener((observableValue, oldVal, newVal) -> {
            newVal.requestFocus();
        });
    }
    
    public Node getCurrentFocusedNode() {
        return currentFocusedNodeProperty.get();
    }

    public void setCurrentFocusedNodeProperty(Node currentFocusedNode) {
        this.currentFocusedNodeProperty.set(currentFocusedNode);
    }
}
