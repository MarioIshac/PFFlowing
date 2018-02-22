package me.theeninja.pfflowing.gui.cardparser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import me.theeninja.pfflowing.flowingregions.Card;

import java.util.List;
import java.util.function.Consumer;

public class ParseCardsTask extends Task<Void> {
    private WebView documentDisplay;
    private final ObservableList<Card> parsedCards = FXCollections.observableArrayList();
    private final KeyCodeCombination PARSE_CARD_PERFORMER = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    private Consumer<TextField> onTextFieldPrompt;

    private void setOnKeySubmission(Node node, EventHandler<KeyEvent> eventHandler) {
        node.setOnKeyPressed(keyEvent -> {
            if (PARSE_CARD_PERFORMER.match(keyEvent))
                eventHandler.handle(keyEvent);
        });
    }

    private String getSelectedText() {
        return (String) documentDisplay.getEngine().executeScript("window.getSelection().toString()");
    }

    private void clearSelection() {
        documentDisplay.getEngine().executeScript("window.getSelection().removeAllRanges()");
    }

    private final static KeyCodeCombination QUIT = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);

    private void onCardParsed(KeyEvent keyEvent) {
        String cardContent = getSelectedText();
        TextField representationRequest = new TextField();
        representationRequest.setOnAction(actionEvent -> {
            Card card = new Card(representationRequest.getText(), cardContent);
            getParsedCards().add(card);

            clearSelection();
            getOnTextFieldPrompt().accept(null);
        });
        getOnTextFieldPrompt().accept(representationRequest);
        representationRequest.requestFocus();
    }

    private void startProcess() {
        setOnKeySubmission(getDocumentDisplay(), this::onCardParsed);
        setOnCancelled(workerStateEvent -> clearSelection());

        this.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
           if (QUIT.match(keyEvent))
               cancelled();
        });
    }

    @Override
    protected Void call() {
        startProcess();
        return null;
    }

    public WebView getDocumentDisplay() {
        return documentDisplay;
    }

    public void setDocumentDisplay(WebView documentDisplay) {
        this.documentDisplay = documentDisplay;
    }

    public ObservableList<Card> getParsedCards() {
        return parsedCards;
    }

    public Consumer<TextField> getOnTextFieldPrompt() {
        return onTextFieldPrompt;
    }

    public void setOnTextFieldPrompt(Consumer<TextField> onTextFieldPrompt) {
        this.onTextFieldPrompt = onTextFieldPrompt;
    }
}
