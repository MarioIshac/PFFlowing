package me.theeninja.pfflowing.gui.cardparser;

import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class CardPossibilityPrompterController implements SingleViewController<VBox> {
    public Label authorLabel;
    public Label dateLabel;
    public Label sourceLabel;
    private Consumer<Card> onParse;
    private Card managedCard;

    @FXML public VBox optionsPane;
    @FXML public ChoosePane<Author> authorOptions;
    @FXML public ChoosePane<Calendar> dateOptions;
    @FXML public ChoosePane<String> sourceOptions;

    private CardPossibilities cardPossibilities;

    public List<ChoosePane> getChoosePanes() {
        return Utils.getOfType(getCorrelatingView().getChildren(), ChoosePane.class);
    }

    private static final Background SELECTED_BACKGROUND = Utils.generateBackgroundOfColor(Color.WHITE);
    private static final Background UNSELECTED_BACKGROUND = Utils.generateBackgroundOfColor(Color.LIGHTGRAY);

    public void initialize(CardPossibilities cardPossibilities) {
        if (cardPossibilities.getAuthors().isEmpty()) {
            getCorrelatingView().getChildren().remove(authorOptions);
            getCorrelatingView().getChildren().remove(authorLabel);
        }
        if (cardPossibilities.getSources().isEmpty()) {
            getCorrelatingView().getChildren().remove(sourceOptions);
            getCorrelatingView().getChildren().remove(authorLabel);
        }
        if (cardPossibilities.getDates().isEmpty()) {
            getCorrelatingView().getChildren().remove(dateOptions);
            getCorrelatingView().getChildren().remove(dateLabel);
        }

        for (int index = 0; index < getChoosePanes().size(); index++) {
            ChoosePane<?> choosePane = getChoosePanes().get(index);
            choosePane.focusedProperty().addListener(((observable, oldValue, newValue) -> {
                System.out.println(choosePane + " gained focus");
                choosePane.setBackground(newValue ? SELECTED_BACKGROUND : UNSELECTED_BACKGROUND);
            }));
            choosePane.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                if (!keyEvent.getCode().isDigitKey())
                    return;
                int targetedIndex = keyEvent.getCode().getCode() - '0';
                System.out.println(targetedIndex);
                choosePane.setSelected(targetedIndex);

                if (Utils.isLastElement(getChoosePanes(), choosePane)) {
                    if (authorOptions.getParent() != null) {
                        getManagedCard().setAuthor(authorOptions.getSelected());
                    }
                    if (dateOptions.getParent() != null) {
                        getManagedCard().setDate(dateOptions.getSelected());
                    }
                    if (sourceOptions.getParent() != null) {
                        getManagedCard().setSource(sourceOptions.getSelected());
                    }
                    getOnParse().accept(getManagedCard());
                }
                else
                    Utils.getRelativeElement(getChoosePanes(), choosePane, 1).requestFocus();
            });
        }
    }

    public void styleChoosePanes() {
        for (ChoosePane<?> choosePane : getChoosePanes()) {

        }
    }

    public CardPossibilities getCardPossibilities() {
        return cardPossibilities;
    }

    public void setCardPossibilities(CardPossibilities cardPossibilities) {
        this.cardPossibilities = cardPossibilities;
    }

    public void addChoosePaneChildren() {
        List<Author> authors = cardPossibilities.getAuthors();

        for (int i = 0; i < authors.size(); i++) {
            Author author = authors.get(i);
            authorOptions.add(new PossibilityLabel<>(author, i));
        }

        List<String> sources = cardPossibilities.getSources();
        for (int i = 0; i < sources.size(); i++) {
            String source = sources.get(i);
            sourceOptions.add(new PossibilityLabel<>(source, i));
        }

        List<Calendar> dates = cardPossibilities.getDates();
        for (int i = 0; i < dates.size(); i++) {
            Calendar date = dates.get(i);
            dateOptions.add(new PossibilityLabel<>(date, i));
        }
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
