package me.theeninja.pfflowing.gui;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.configuration.GlobalConfiguration;
import me.theeninja.pfflowing.flowingregions.Card;

public class CardDisplayController implements SingleViewController<VBox> {
    @FXML public VBox cardDisplay;

    public void clear() {
        getCorrelatingView().getChildren().clear();
    }

    public void add(Card card) {
        VBox cardContainer = new VBox();
        Text authorText = new Text(card.getAuthor());
        Text date = new Text(GlobalConfiguration.DATE_FORMAT.format(card.getDate()));
        cardContainer.getChildren().addAll(authorText, date);

    }

    /**
     * Provides the one, main view that is represented by this controller.
     *
     * @return The one, main view correlating to the controller.
     */
    @Override
    public VBox getCorrelatingView() {
        return this.cardDisplay;
    }
}
