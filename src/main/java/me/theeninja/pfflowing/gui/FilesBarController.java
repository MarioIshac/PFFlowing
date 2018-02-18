package me.theeninja.pfflowing.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import me.theeninja.pfflowing.PFFlowing;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.tournament.Round;
import me.theeninja.pfflowing.tournament.Tournament;
import me.theeninja.pfflowing.utils.Utils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class FilesBarController implements Initializable, SingleViewController<HBox> {

    private Tournament boundTournament;

    @FXML
    public HBox filesBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @Override
    public HBox getCorrelatingView() {
        return filesBar;
    }

    private static final String DELETE_SYMBOL = "X";
    private static final String SELECTION_LABEL_CLASS = "selectionLabel";
    private static final String CROSSOUT_LABEL_CLASS = "crossOutLabel";

    public void addRoundSelector(String string, Round round) {
        Label selectionLabel = new Label(string);
        selectionLabel.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            switch (mouseEvent.getClickCount()) {
                case 1: {
                    getBoundTournament().getSelectedRound().
                    break;
                }
                default: return;
            }
        });
        selectionLabel.getStyleClass().add(SELECTION_LABEL_CLASS);

        Label crossOutLabel = new Label(DELETE_SYMBOL);
        crossOutLabel.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            getCorrelatingView().getChildren().remove(crossOutLabel);
        });
        crossOutLabel.getStyleClass().add(CROSSOUT_LABEL_CLASS);
    }

    private Round getNewFlowingGridControllers() {
        AffirmativeFlowingGridController aff = Utils.getCorrelatingController("/aff_flowing_pane.fxml");
        NegationFlowingGridController neg = Utils.getCorrelatingController("/neg_flowing_pane.fxml");
        return new Round(aff, neg);

    }

    public Tournament getBoundTournament() {
        return boundTournament;
    }

    public void setBoundTournament(Tournament boundTournament) {
        this.boundTournament = boundTournament;
        getBoundTournament().getRounds().addListener(Utils.generateListChangeListener(
            this::addRoundSelector,
            this::addRoundSelector
        ));
    }
}
