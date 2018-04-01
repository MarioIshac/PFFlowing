package me.theeninja.pfflowing.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.utils.Utils;

import java.net.URL;
import java.util.*;

public class CardsDetailerController implements Initializable, Detailer, SingleViewController {
    private final FlowingRegion flowingRegion;

    private final Map<KeyCodeCombination, Runnable> KEYCOMBS = Map.of(
            NEXT, () -> {
                int currentIndex = getFlowingRegion().getAssociatedCards().indexOf(getCurrentCard());

                if (currentIndex == getFlowingRegion().getAssociatedCards().size() - 1)
                    return;

                next();
            },
            PREVIOUS, () -> {
                int currentIndex = getFlowingRegion().getAssociatedCards().indexOf(getCurrentCard());

                if (currentIndex == 0)
                    return;

                previous();
            },
            ZOOM_IN, this::zoomIn,
            ZOOM_OUT, this::zoomOut
    );

    private static final double ZOOM_FACTOR = 1.1;

    private void zoomOut() {
        zoomChange(1 / ZOOM_FACTOR);
    }

    private void zoomIn() {
        zoomChange(ZOOM_FACTOR);
    }

    private void zoomChange(double factor) {
        WebView currentWebView = cardWebViews.get(getCurrentCard());
        double currentZoom = currentWebView.getZoom();
        double newZoom = currentZoom * factor;
        currentWebView.setZoom(newZoom);
    }

    private void onCurrentCardChanged(ObservableValue<? extends Card> observable, Card oldValue, Card newValue) {
        WebView webView = cardWebViews.get(newValue);

        if (newValue != null) {
            webViewContainer.getChildren().setAll(Collections.singleton(webView));

            int newIndex = getFlowingRegion().getAssociatedCards().indexOf(newValue);

            next.setVisible(true);
            previous.setVisible(true);

            if (newIndex == getFlowingRegion().getAssociatedCards().size() - 1)
                next.setVisible(false);
            if (newIndex == 0)
                previous.setVisible(false);
        }
        else {
            webViewContainer.getChildren().clear();
        }
    }

    @FXML
    public VBox webViewContainer;

    @FXML
    public Button previous;

    @FXML
    public Button next;

    private Map<Card, WebView> cardWebViews = new HashMap<>();
    private ObjectProperty<Card> currentCard = new SimpleObjectProperty<>();

    @FXML
    public VBox root;

    public CardsDetailerController(FlowingRegion flowingRegion) {
        this.flowingRegion = flowingRegion;
    }

    @FXML
    public void next() {
        int currentIndex = getFlowingRegion().getAssociatedCards().indexOf(getCurrentCard());
        int nextIndex = currentIndex + 1;
        Card nextCard = getFlowingRegion().getAssociatedCards().get(nextIndex);
        setCurrentCard(nextCard);
    }

    @FXML
    public void previous() {
        int currentIndex = getFlowingRegion().getAssociatedCards().indexOf(getCurrentCard());
        int previousIndex = currentIndex - 1;
        Card previousCard = getFlowingRegion().getAssociatedCards().get(previousIndex);
        setCurrentCard(previousCard);
    }

    private static final KeyCodeCombination NEXT = new KeyCodeCombination(KeyCode.RIGHT);
    private static final KeyCodeCombination PREVIOUS = new KeyCodeCombination(KeyCode.LEFT);

    private void onKeyPressed(KeyEvent keyEvent) {
        KEYCOMBS.forEach((keyCodeCombination, runnable) -> {
            if (keyCodeCombination.match(keyEvent)) {
                runnable.run();
                keyEvent.consume();
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);

        currentCardProperty().addListener(this::onCurrentCardChanged);

        getFlowingRegion().getAssociatedCards().addListener(Utils.generateListChangeListener(
            this::onAssociatedCardAdd,
            this::onAssociatedCardRemove
        ));
    }

    private final static KeyCodeCombination ZOOM_IN = new KeyCodeCombination(KeyCode.PERIOD);
    private final static KeyCodeCombination ZOOM_OUT = new KeyCodeCombination(KeyCode.COMMA);

    public FlowingRegion getFlowingRegion() {
        return flowingRegion;
    }

    private void onAssociatedCardAdd(Card card) {
        System.out.println("Associated card added");

        WebView webView = new WebView();
        webView.fontScaleProperty().bind(EFlow.getInstance().getConfiguration().getFontScale().valueProperty());
        webView.setPrefHeight(400);
        webView.getEngine().loadContent(card.getHTMLContent());

        cardWebViews.put(card, webView);
    }

    private void onAssociatedCardRemove(Card card) {
        System.out.println("Associated card removed");

        cardWebViews.remove(card);
    }

    public Card getCurrentCard() {
        return currentCard.get();
    }

    public ObjectProperty<Card> currentCardProperty() {
        return currentCard;
    }

    public void setCurrentCard(Card currentCard) {
        this.currentCard.set(currentCard);
    }

    @Override
    public boolean hasDetail() {
        return !getFlowingRegion().getAssociatedCards().isEmpty();
    }

    @Override
    public Node getCorrelatingView() {
        return root;
    }
}
