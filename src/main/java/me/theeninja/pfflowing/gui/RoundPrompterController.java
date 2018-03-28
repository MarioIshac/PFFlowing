package me.theeninja.pfflowing.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.theeninja.pfflowing.FlowApp;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.UseType;

import java.net.URL;
import java.util.ResourceBundle;

public class RoundPrompterController implements SingleViewController<VBox>, Initializable {
    private final FlowController flowController;

    @FXML
    public Button finishButton;

    public RoundPrompterController(FlowController flowController) {
        this.flowController = flowController;
    }

    @FXML
    public VBox promptBox;

    @FXML
    public TextField roundNameField;

    @FXML
    public CheckBox newTournamentCheckBox;

    @FXML
    public HBox tournamentNameBox;

    @FXML
    public TextField tournamentNameField;

    @FXML
    public ComboBox<Side> sideChooser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Do not present option for new tournament name if user does not wish to create
        // new tournament
        tournamentNameBox.visibleProperty().bind(newTournamentCheckBox.selectedProperty());

        sideChooser.getItems().addAll(Side.values());
    }

    @FXML
    public void finish() {
        String roundName = roundNameField.getText();
        Side side = sideChooser.getValue();

        Runnable options[][] = {
                {
                        () -> { // add round to new tournament with something in use
                            FlowApp newApplication = new FlowApp();
                            Stage allocatedStage = new Stage();
                            newApplication.start(allocatedStage);
                            newApplication.getFlowController().addRound(roundName, side);
                            newApplication.getFlowController().setUseType(UseType.TOURNAMENT);
                        },

                        () -> { // add round to new tournament with nothing in use
                            getFlowController().addRound(roundName, side);
                            getFlowController().setUseType(UseType.TOURNAMENT);
                        }
                },

                {
                        () -> { // don't add round to new tournament with something in use
                            FlowApp newApplication = new FlowApp();
                            Stage allocatedStage = new Stage();
                            newApplication.start(allocatedStage);
                            newApplication.getFlowController().addRound(roundName, side);
                            newApplication.getFlowController().setUseType(UseType.ROUND);
                        },

                        () -> { // don't add round to new tournament with nothing in use
                            getFlowController().addRound(roundName, side);
                            getFlowController().setUseType(UseType.ROUND);
                        }
                }
        };

        options[newTournamentCheckBox.isSelected()         ? 0 : 1]
               [getFlowController().getUseType().isInUse() ? 0 : 1].run();
    }

    /**
     * 4 options:
     *
     * <ol>
     *      <li> Add new round to existing tournament in current stage </li>
     *      <li> Add new round without tournament to current stage </li>
     *      <li> Add new round to new tournament in new stage </li>
     *      <li> Add new round without tournament to new stage </li>
     * </ol>
     */


    public FlowController getFlowController() {
        return flowController;
    }

    @Override
    public VBox getCorrelatingView() {
        return promptBox;
    }
}
