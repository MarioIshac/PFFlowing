package me.theeninja.pfflowing.gui;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.theeninja.pfflowing.gui.cardparser.CardParserController;
import me.theeninja.pfflowing.utils.Utils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class NavigatorController implements Initializable {
    private static NavigatorController fxmlInstance;

    private boolean doesFlowFileExist;

    public static NavigatorController getFxmlInstance() {
        return fxmlInstance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxmlInstance = this;

        setDoesFlowFileExist(false);
    }

    public void newFlow(ActionEvent actionEvent) {

    }

    public void openRound() {
        try {
            FlowController.getFXMLInstance().openRound();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openTournament() {
        try {
            FlowController.getFXMLInstance().openTournament();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFlow() {

    }

    public void saveFlowAs(ActionEvent actionEvent) {
    }

    public void printFlow(ActionEvent actionEvent) {

    }

    public void emailFlow(ActionEvent actionEvent) {

    }

    public boolean doesFlowFileExist() {
        return doesFlowFileExist;
    }

    public void setDoesFlowFileExist(boolean doesFlowFileExist) {
        this.doesFlowFileExist = doesFlowFileExist;
    }

    public void undo(ActionEvent actionEvent) {

    }

    public void redo(ActionEvent actionEvent) {

    }

    public void selectAll(ActionEvent actionEvent) {

    }

    public void openParserPopup() {
        CardParserController cardParserController = Utils.getCorrelatingController("/card_parser_gui/card_parser.fxml");
        System.out.println("debug");
        Stage stage = new Stage();
        Scene scene = new Scene(cardParserController.getCorrelatingView());
        stage.setScene(scene);
        stage.show();
        cardParserController.setAssociatedStage(stage);

        Path path = cardParserController.askForFile();
        cardParserController.loadPath(path);
    }


    public void newRound() {
        FlowController.getFXMLInstance().addRound();
    }

    public void newTournament() {
        FlowController.getFXMLInstance().newTournament();
    }
}
