package me.theeninja.pfflowing.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;

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
    public ComboBox<Side> sideChooser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sideChooser.getItems().addAll(Side.values());
    }

    public void finish() {
        String roundName = roundNameField.getText();
        Side side = sideChooser.getValue();

        Round round = new Round(roundName, side);

        getFlowController().addRound(round);
    }

    public FlowController getFlowController() {
        return flowController;
    }

    @Override
    public VBox getCorrelatingView() {
        return promptBox;
    }
}
