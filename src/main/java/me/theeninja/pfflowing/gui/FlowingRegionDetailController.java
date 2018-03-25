package me.theeninja.pfflowing.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowingregions.Card;

import java.net.URL;
import java.util.ResourceBundle;

public class FlowingRegionDetailController implements SingleViewController<VBox>, Initializable {
    private final FlowingRegion flowingRegion;

    @FXML
    public VBox cardDetailsBox;

    FlowingRegionDetailController(FlowingRegion flowingRegion) {
        this.flowingRegion = flowingRegion;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (Card card : getFlowingRegion().getAssociatedCards()) {
            WebView webView = new WebView();
            webView.getEngine().loadContent(card.getHTMLContent());
            getCorrelatingView().getChildren().add(webView);
        }
    }

    public FlowingRegion getFlowingRegion() {
        return flowingRegion;
    }

    @Override
    public VBox getCorrelatingView() {
        return cardDetailsBox;
    }
}
