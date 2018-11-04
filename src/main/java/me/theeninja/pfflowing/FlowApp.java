package me.theeninja.pfflowing;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import me.theeninja.pfflowing.bluetooth.EFlowConnector;
import me.theeninja.pfflowing.gui.*;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;

import java.io.IOException;
import java.util.logging.Logger;

public class FlowApp extends Application {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    private ActionManager actionManager;

    private Scene scene;

    private Stage stage;
    private FlowController flowController;

    public Logger getLogger() {
        return logger;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public FlowController getFlowController() {
        return flowController;
    }

    public void setFlowController(FlowController flowController) {
        this.flowController = flowController;
    }

    public void toggleFullscreen() {
        getStage().setFullScreen(!getStage().isFullScreen());
    }

    public static final String APPLICATION_STAGE_TITLE = "Public Forum Flowing";

    @Override
    public void start(Stage stage) {
        setStage(stage);

        FlowController flowController = new FlowController(this);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/flow/flow.fxml"));
        fxmlLoader.setController(flowController);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.flowController = flowController;

        this.scene = new Scene(flowController.getCorrelatingView());

        getStage().setScene(scene);
        getStage().setTitle(APPLICATION_STAGE_TITLE);
        getStage().setFullScreen(true);
        getStage().setFullScreenExitKeyCombination(KeyCodeCombination.NO_MATCH);
        getStage().show();

        EFlow.setAsFullscreenToggler(getStage());
    }

    public Scene getScene() {
        return scene;
    }

    public Stage getStage() {
        return this.stage;
    }
}
