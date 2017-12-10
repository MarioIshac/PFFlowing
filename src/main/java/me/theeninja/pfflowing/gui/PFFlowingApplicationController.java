package me.theeninja.pfflowing.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.Utils;

import java.net.URL;
import java.util.ResourceBundle;

public class PFFlowingApplicationController implements Initializable, SingleViewController<BorderPane> {
    @FXML public BorderPane pfFlowingMain;

    private ObjectProperty<Node> currentFocusedNodeProperty = new SimpleObjectProperty<>();

    /* private final List<Node> mainNodes = Arrays.asList(
            pfFlowingMain.getLeft(),
            pfFlowingMain.getCenter()
    );

    public void cycleMainNodeFocus() {
        int baseIndex = mainNodes.indexOf(getCurrentFocusedNodeProperty());
        int newIndex = baseIndex == mainNodes.size() - 1 ? 0 : baseIndex + 1;
        setCurrentFocusedNodeProperty(mainNodes.get(newIndex));
    } */

    @Override
    public BorderPane getCorrelatingView() {
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
