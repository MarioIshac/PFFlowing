package me.theeninja.pfflowing;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import me.theeninja.pfflowing.gui.*;

import java.util.logging.Logger;

public class PFFlowing extends Application {
    public static final String APPLICATION_TITLE = "Public Forum Flowing";

    private final Logger logger = Logger.getLogger(PFFlowing.class.getSimpleName());

    public static void main(String[] args) {
        PFFlowing.launch(PFFlowing.class);
    }

    private ActionManager actionManager;

    private Scene scene;
    private Stage stage;

    private static PFFlowing instance;

    public static PFFlowing getInstance() {
        return instance;
    }

    public void toggleFullscreen() {
        getStage().setFullScreen(!getStage().isFullScreen());
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        scene = new Scene(FlowController.getFXMLInstance().getCorrelatingView());
        instance = this;

        stage.setScene(scene);
        stage.setTitle(APPLICATION_TITLE);
        stage.setFullScreen(true);
        stage.setFullScreenExitKeyCombination(KeyCodeCombination.NO_MATCH);
        stage.show();

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
