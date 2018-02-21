package me.theeninja.pfflowing.gui;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;
import me.theeninja.pfflowing.tournament.Tournament;
import me.theeninja.pfflowing.utils.Utils;

import java.net.URL;
import java.util.ResourceBundle;

public class FlowController implements Initializable, SingleViewController<FlowingPane> {
    @FXML public FlowingPane pfFlowingMain;
    @FXML public MenuBar navigator;
    @FXML public HBox notificationDisplay;
    @FXML public TabPane roundsBar;

    @Override
    public FlowingPane getCorrelatingView() {
        return pfFlowingMain;
    }

    private static FlowController fxmlInstance;

    public static FlowController getFXMLInstance() {
        if (fxmlInstance == null) {
            fxmlInstance = Utils.getCorrelatingController("/flow.fxml");
        }
        return fxmlInstance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navigator.setPrefHeight(Region.USE_PREF_SIZE);
        roundsBar.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        roundsBar.getSelectionModel().selectedItemProperty().addListener(this::onSelectedTabChanged);

        roundsBar.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (KeyCodeCombinationUtils.SWITCH_SPEECHLIST.match(keyEvent)) {
                Round selectedRound = getTournament().getSelectedRound();
                selectedRound.setDisplayedSide(selectedRound.getDisplayedSide().getOpposite());
            }
        });

        roundsBar.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                roundsBar.getSelectionModel().getSelectedItem().getContent().requestFocus();
            }
        }));
    }

    private static final KeyCodeCombination FINISH = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    private static final String SELECTION_LABEL_CLASS = "selectionLabel";
    private static final String SELECTED_SELECTION_LABEL_CLASS = "selectedSelectionLabel";

    public void addRound() {
        Stage configStage = new Stage();
        VBox configLayout = new VBox();
        Scene configScene = new Scene(configLayout);
        configStage.setScene(configScene);

        Label nameLabel = new Label("Name");
        TextField nameField = new TextField();

        CheckBox inTournamentCheckbox = new CheckBox();
        inTournamentCheckbox.setText("New Tournament");

        Label tournamentLabel = new Label("Tournament");
        TextField tournamentField = new TextField();

        HBox tournamentRequest = new HBox(tournamentLabel, tournamentField);
        tournamentRequest.visibleProperty().bind(inTournamentCheckbox.selectedProperty());

        ComboBox<Side> comboBox = new ComboBox<>();
        comboBox.getItems().add(Side.AFFIRMATIVE);
        comboBox.getItems().add(Side.NEGATION);
        comboBox.setPromptText("Side");

        Button finish = new Button("Finish");

        configLayout.getChildren().addAll(
                new HBox(nameLabel, nameField),
                inTournamentCheckbox,
                tournamentRequest,
                comboBox,
                finish
        );

        Runnable options[][] = {
            {
                () -> { // create tournment + no tournament exists
                    setTournament(newTournament(tournamentField.getText()));

                    roundsBar.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
                        RoundTab roundTab = (RoundTab) newValue;
                        getTournament().setSelectedRound(roundTab.getRound());
                        System.out.println("new round" + roundTab.getRound().getName());
                    }));

                    addRoundToTournament(nameField.getText(), comboBox.getValue());
                    configStage.hide();
                },

                () -> { // create tournament + tournament exists

                }
            },

            {
                () -> { // don't create tournament + no tournament exists

                },

                () -> { // don't create tournament + tournament exists
                    addRoundToTournament(nameField.getText(), comboBox.getValue());
                    configStage.hide();
                }
            }
        };

        Runnable onSubmission = () -> {
            if (nameField.getText().isEmpty())
                nameField.setPromptText("Round name?");
            else
                options[inTournamentCheckbox.isSelected() ? 0 : 1]
                        [getTournament() == null ? 0 : 1]
                        .run();
        };

        configLayout.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            if (FINISH.match(keyEvent))
                onSubmission.run();
        });

        finish.addEventHandler(ActionEvent.ACTION, actionEvent ->
                onSubmission.run()
        );

        configStage.show();
    }


    private Tournament tournament;

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    private void onSelectedControllerChanged(ObservableValue<? extends FlowingGridController> observableController, FlowingGridController oldController, FlowingGridController newController) {
        System.out.println("Controller switched for " + newController.getCorrelatingView().getChildren());
        //roundsBar.setOnKeyPressed(newController.getHandleKeyEvent());
        newController.getCorrelatingView().requestFocus();
    }

    private void onSelectedTabChanged(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        System.out.println("Tab changed");

        if (oldValue != null) {
            RoundTab oldRoundTab = (RoundTab) oldValue;
            System.out.println("Listener removed from " + oldRoundTab.getRound().getName());
            oldRoundTab.getRound().selectedControllerProperty().removeListener(this::onSelectedControllerChanged);

        }
        RoundTab newRoundTab = (RoundTab) newValue;
        System.out.println("Listener added to " + newRoundTab.getRound().getName());

        newRoundTab.getRound().selectedControllerProperty().addListener(this::onSelectedControllerChanged);

        newRoundTab.getContent().requestFocus();
        //roundsBar.setOnKeyPressed(newRoundTab.getRound().getSelectedController().getHandleKeyEvent());
    }

    private void addRoundToTournament(String roundName, Side side) {
        Round round = new Round(side);
        getTournament().getRounds().add(round);
        round.setName(roundName);
        round.setDisplayedSide(round.getSide());
    }

    private Tournament newTournament(String tournamentName) {
        Tournament tournament = new Tournament(tournamentName);

        tournament.getRounds().addListener(Utils.generateListChangeListener(
                round -> {
                    RoundTab tab = new RoundTab(round);
                    roundsBar.getTabs().add(tab);
                },
                round -> {

                }
        ));

        return tournament;
    }
}
