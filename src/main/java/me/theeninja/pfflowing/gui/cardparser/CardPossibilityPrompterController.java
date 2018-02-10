package me.theeninja.pfflowing.gui.cardparser;

import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowingregions.Author;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.utils.Utils;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class CardPossibilityPrompterController implements SingleViewController<VBox> {
    public Label authorLabel;
    public Label dateLabel;
    public Label sourceLabel;
    private Consumer<Card> onParse;
    private Card managedCard;

    @FXML public VBox optionsPane;

    public List<ChoosePane> getChoosePanes() {
        return Utils.getOfType(getCorrelatingView().getChildren(), ChoosePane.class);
    }

    private static final Background SELECTED_BACKGROUND = Utils.generateBackgroundOfColor(Color.WHITE);
    private static final Background UNSELECTED_BACKGROUND = Utils.generateBackgroundOfColor(Color.LIGHTGRAY);

    private <T> void handle(List<T> list, String labelText, Function<String, T> defFunc, Consumer<T> onSelection) {
        Label label = new Label(labelText);
        getCorrelatingView().getChildren().add(label);

        ChoosePane<T> choosePane = new ChoosePane<>();

        for (int i = 0; i < list.size(); i++) {
            T value = list.get(i);
            choosePane.add(new PossibilityLabel<>(value, i));
        }

        choosePane.focusedProperty().addListener((observable, oldValue, newValue) ->
            choosePane.setBackground(newValue ? SELECTED_BACKGROUND : UNSELECTED_BACKGROUND)
        );

        choosePane.selectedProperty().addListener((observable, oldValue, newValue) ->
            onSelection.accept(newValue)
        );

        choosePane.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            System.out.println("Has been called");

            if (keyEvent.getCode() == KeyCode.TAB) {
                TextField textField = new TextField();
                choosePane.getChildren().add(textField);
                textField.setPromptText(labelText + "?");
                textField.requestFocus();
                textField.setOnAction(actionEvent -> {
                    choosePane.setSelected(defFunc.apply(textField.getText()));
                    next(choosePane);
                });
            }

            if (!keyEvent.getCode().isDigitKey())
                return;

            int targetedIndex = keyEvent.getCode().getCode() - '0';
            choosePane.setSelected(targetedIndex);
            next(choosePane);
        });

        getCorrelatingView().getChildren().add(choosePane);
    }

    private void next(ChoosePane<?> choosePane) {
        if (Utils.isLastElement(getChoosePanes(), choosePane))
            getOnParse().accept(getManagedCard());

        else
            Utils.getRelativeElement(getChoosePanes(), choosePane, 1).requestFocus();
    }

    public void start(CardPossibilities cardPossibilities) {
        handle(cardPossibilities.getAuthors(), "Authors", string -> {
            String[] nameTokens = string.trim().split("\\s+");
            if (nameTokens.length == 1) {
                return new Author(nameTokens[0]);
            }
            else if (nameTokens.length == 2) {
                return new Author(nameTokens[1]);
            }
            else {
                throw new Error("lol");
            }
        }, getManagedCard()::setAuthor);
        handle(cardPossibilities.getDates(), "Dates", Utils::calendarOf, getManagedCard()::setDate);
        handle(cardPossibilities.getSources(), "Sources", string -> string, getManagedCard()::setSource);
    }

    @Override
    public VBox getCorrelatingView() {
        return optionsPane;
    }

    public Consumer<Card> getOnParse() {
        return onParse;
    }

    public void setOnParse(Consumer<Card> onParse) {
        this.onParse = onParse;
    }

    public Card getManagedCard() {
        return managedCard;
    }

    public void setManagedCard(Card managedCard) {
        this.managedCard = managedCard;
    }
}
