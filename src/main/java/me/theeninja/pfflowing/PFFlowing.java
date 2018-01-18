package me.theeninja.pfflowing;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import me.theeninja.pfflowing.gui.*;
import me.theeninja.pfflowing.utils.Utils;

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

    private AffirmativeFlowingGridController affirmativeFlowingGridController;
    private NegationFlowingGridController negationFlowingGridController;

    public void switchSpeechList() {
        if (getAffirmativeFlowingGridController().isShown()) {
            getAffirmativeFlowingGridController().hide();
            getNegationFlowingGridController().show();
        }
        else {
            getNegationFlowingGridController().hide();
            getAffirmativeFlowingGridController().show();
        }
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        scene = new Scene(PFFlowingApplicationController.getFXMLInstance().getCorrelatingView());
        instance = this;

        stage.setScene(scene);
        stage.setTitle(APPLICATION_TITLE);
        stage.setFullScreen(true);
        stage.setFullScreenExitKeyCombination(KeyCodeCombination.NO_MATCH);
        stage.show();

        setAffirmativeFlowingGridController(Utils.getCorrelatingController("/aff_flowing_pane.fxml"));
        setNegationFlowingGridController(Utils.getCorrelatingController("/neg_flowing_pane.fxml"));

        getAffirmativeFlowingGridController().show();

        setActionManager(new ActionManager());

        /* String directory = System.getProperty("user.home") + "/Desktop/DebateCards";
        System.out.println(directory);
        Path path = Paths.get(directory);

        if (Files.isDirectory(path)) {
            CardsProcessor cardsProcessor = new CardsProcessor(path.toString());
            List<Card> cardsToUse = cardsProcessor.findCards();

            CardSelectorController.getFXMLInstance().setDisplay(cardsToUse);
        }
        else {
            throw new FileNotFoundException("The directory " + directory + " is not valid.");
        } */
    }

    public Scene getScene() {
        return scene;
    }

    public Stage getStage() {
        return this.stage;
    }

    public AffirmativeFlowingGridController getAffirmativeFlowingGridController() {
        return affirmativeFlowingGridController;
    }

    public void setAffirmativeFlowingGridController(AffirmativeFlowingGridController affirmativeFlowingGridController) {
        this.affirmativeFlowingGridController = affirmativeFlowingGridController;
    }

    public NegationFlowingGridController getNegationFlowingGridController() {
        return negationFlowingGridController;
    }

    public void setNegationFlowingGridController(NegationFlowingGridController negationFlowingGridController) {
        this.negationFlowingGridController = negationFlowingGridController;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    private void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }
}
