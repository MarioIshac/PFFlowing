package me.theeninja.pfflowing.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.utils.Utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class FlowingRegionDetailController implements SingleViewController<VBox>, Initializable {
    private final FlowingRegion flowingRegion;

    private Map<Card, WebView> cardWebViewMap = new HashMap<>();

    @FXML
    public VBox cardDetailsBox;

    public FlowingRegionDetailController(FlowingRegion flowingRegion) {
        this.flowingRegion = flowingRegion;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cardDetailsBox.setBackground(Utils.generateBackgroundOfColor(Color.RED));

        getFlowingRegion().getAssociatedCards().addListener(Utils.generateListChangeListener(
            this::onAssociatedCardAdd,
            this::onAssociatedCardRemove
        ));
    }

    public FlowingRegion getFlowingRegion() {
        return flowingRegion;
    }

    @Override
    public VBox getCorrelatingView() {
        return cardDetailsBox;
    }

    private void onAssociatedCardAdd(Card card) {
        System.out.println("Associated card added");

        WebView webView = new WebView();
        webView.fontScaleProperty().bind(EFlow.getInstance().getConfiguration().getFontScale().valueProperty());
        webView.getEngine().loadContent(card.getHTMLContent());

        getCorrelatingView().getChildren().add(webView);
        cardWebViewMap.put(card, webView);
    }

    private void onAssociatedCardRemove(Card card) {
        System.out.println("Associated card removed");

        WebView webView = cardWebViewMap.get(card);
        getCorrelatingView().getChildren().remove(webView);
        cardWebViewMap.remove(card);
    }
}
