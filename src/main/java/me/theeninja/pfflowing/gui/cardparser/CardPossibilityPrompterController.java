package me.theeninja.pfflowing.gui.cardparser;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.utils.Utils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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

    private <T> void handle(Set<T> set, String labelText, Function<String, T> defFunc, Consumer<T> onSelection) {
        Label label = new Label(labelText);
        getCorrelatingView().getChildren().add(label);

        ChoosePane<T> choosePane = new ChoosePane<>();

        int index = 0;

        for (T t : set)
            choosePane.add(new PossibilityLabel<T>(t, index++, "[%d]", ""));

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
            return "Mario";
        }, getManagedCard()::setAuthor);
        handle(cardPossibilities.getDates(), "Dates", string -> string, getManagedCard()::setDate);
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
