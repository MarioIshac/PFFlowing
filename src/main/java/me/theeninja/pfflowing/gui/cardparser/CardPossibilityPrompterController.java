package me.theeninja.pfflowing.gui.cardparser;

import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.SingleViewController;

import java.net.URL;
import java.util.ResourceBundle;

public class CardPossibilityPrompterController implements Initializable, SingleViewController<VBox> {
    public VBox optionsPane;
    public HBox authorOptions;
    public HBox dateOptions;
    public VBox sourceOptions;

    private CardPossibilities cardPossibilities;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public CardPossibilities getCardPossibilities() {
        return cardPossibilities;
    }

    public void setCardPossibilities(CardPossibilities cardPossibilities) {
        this.cardPossibilities = cardPossibilities;
    }

    @Override
    public VBox getCorrelatingView() {
        return optionsPane;
    }
}
