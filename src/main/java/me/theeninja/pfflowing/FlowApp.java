package me.theeninja.pfflowing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import me.theeninja.pfflowing.gui.*;

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

    public void setScene(Scene scene) {
        this.scene = scene;
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

        setFlowController(flowController);
        setScene(new Scene(flowController.getCorrelatingView()));

        getStage().setScene(scene);
        getStage().setTitle(APPLICATION_STAGE_TITLE);
        getStage().setFullScreen(true);
        getStage().setFullScreenExitKeyCombination(KeyCodeCombination.NO_MATCH);
        getStage().show();

        setActionManager(new ActionManager());
    }

    public Scene getScene() {
        return scene;
    }

    public Stage getStage() {
        return this.stage;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    private void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }
}
