package me.theeninja.pfflowing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionAdapter;
import me.theeninja.pfflowing.gui.*;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;
import me.theeninja.pfflowing.tournament.Tournament;
import me.theeninja.pfflowing.utils.Utils;
import org.hildan.fxgson.FxGson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    private FileChooser fileChooser;

    public FileChooser getFileChooser() {
        return fileChooser;
    }

    private static final KeyCodeCombination FINISH = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

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

        setFileChooser(new FileChooser());

        FileChooser.ExtensionFilter eflowExtensionFilter = new FileChooser.ExtensionFilter("EFlow files (*.eflow)", "*.eflow");
        getFileChooser().getExtensionFilters().add(eflowExtensionFilter);

        setActionManager(new ActionManager());

        initializeGSON();

        FlowController.getFXMLInstance().addRound();
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

    private void initializeGSON() {
        setGson(
                FxGson.coreBuilder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .registerTypeAdapter(FlowingRegion.class, new FlowingRegionAdapter())
                        .registerTypeAdapter(FlowingGrid.class, new FlowingGridAdapter())
                        .setPrettyPrinting()
                        .create()
        );
    }

    private Gson gson;

    private void setGson(Gson gson) {
        this.gson = gson;
    }

    private static final String FILE_EXTENSION = "eflow";
    private static final String SAVE_TITLE = "Save an EFlow";

    private File getEFlowTypeFile(File file) {
        System.out.println(file.getAbsolutePath());
        if (!file.getAbsolutePath().endsWith("." + FILE_EXTENSION))
            return new File(file.getAbsoluteFile() + "." + FILE_EXTENSION);
        else
            return file;
    }

    public void saveAs() throws IOException {
        getFileChooser().setTitle(SAVE_TITLE);
        File file = getFileChooser().showSaveDialog(this.getStage());

        // no file chosen
        if (file == null)
            return; // assume that user cancelled saving

        File eflowFile = getEFlowTypeFile(file);

        Path path = eflowFile.toPath();
    }

    private static final String OPEN_TITLE = "Open an EFlow";

    private static final Type LIST_TYPE = new TypeToken<List<String>>(){}.getType();


    public void open() throws IOException {
        getFileChooser().setTitle(OPEN_TITLE);
        File file = getFileChooser().showOpenDialog(this.getStage());

        // no file chosen
        if (file == null)
            return; // assume that user cancelled opening

        File eflowFile = getEFlowTypeFile(file);

        Path path = eflowFile.toPath();
        byte[] jsonBytes = Files.readAllBytes(path);
        String json = new String(jsonBytes);
        FlowingGrid flowingGrids = getGSON().fromJson(json, FlowingGrid.class);
    }

    public Gson getGSON() {
        return gson;
    }

    public void setFileChooser(FileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }
}
