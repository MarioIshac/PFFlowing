package me.theeninja.pfflowing;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import me.theeninja.pfflowing.card.Card;
import me.theeninja.pfflowing.card.CardsProcessor;
import me.theeninja.pfflowing.gui.*;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

public class PFFlowing extends Application {
    public static final String APPLICATION_TITLE = "Public Forum Flowing";

    private final Logger logger = Logger.getLogger(PFFlowing.class.getSimpleName());

    public static void main(String[] args) {
        PFFlowing.launch(PFFlowing.class);
    }

    private Scene scene;
    private Stage stage;

    private static PFFlowing instance;

    public static PFFlowing getInstance() {
        return instance;
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        scene = new Scene(PFFlowingApplicationController.getFXMLInstance().getCorrelatingView());
        // FlowingColumnsController.getFXMLInstance().generateLineLinksListener();
        instance = this;

        stage.setScene(scene);
        stage.setTitle(APPLICATION_TITLE);
        stage.setFullScreen(true);
        scene.setOnKeyReleased(FlowingColumnsController.getFXMLInstance());
        stage.setFullScreenExitKeyCombination(KeyCodeCombination.NO_MATCH);
        stage.show();

        FlowingColumnsController.getFXMLInstance().getCorrelatingView().requestFocus();
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
}
