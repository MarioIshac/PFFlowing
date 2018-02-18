package me.theeninja.pfflowing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionAdapter;
import me.theeninja.pfflowing.gui.*;
import me.theeninja.pfflowing.utils.Utils;
import org.hildan.fxgson.FxGson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private FlowingGridControllers flowingGridControllers;
    private FilesBarController filesBarController;
    private FileChooser fileChooser;

    public FileChooser getFileChooser() {
        return fileChooser;
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

        setFlowingGridControllers(new FlowingGridControllers(
            Utils.getCorrelatingController("/aff_flowing_pane.fxml"),
            Utils.getCorrelatingController("/neg_flowing_pane.fxml")
        ));
        setFileChooser(new FileChooser());

        FileChooser.ExtensionFilter eflowExtensionFilter = new FileChooser.ExtensionFilter("EFlow files (*.eflow)", "*.eflow");
        getFileChooser().getExtensionFilters().add(eflowExtensionFilter);

        setActionManager(new ActionManager());

        initializeGSON();

        setFilesBarController(Utils.getCorrelatingController("/files_bar.fxml"));
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
                        .registerTypeAdapter(FlowingGrids.class, new RoundAdapter())
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
        String json = getGSON().toJson(getFlowingGridControllers());
        Files.write(path, json.getBytes());
    }

    private static final String OPEN_TITLE = "Open an EFlow";

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
        FlowingGrid flowingGrids = getGSON().fromJson(json, FlowingGrids.class);
        flowingGrids.putIntoControllers(getFlowingGridControllers());
        getFilesBarController().addLabel(path.getFileName().toString(), flowingGrids);
    }

    public Gson getGSON() {
        return gson;
    }

    public FilesBarController getFilesBarController() {
        return filesBarController;
    }

    public void setFilesBarController(FilesBarController filesBarController) {
        this.filesBarController = filesBarController;
    }

    public FlowingGridController getFlowingGridControllers() {
        return flowingGridControllers;
    }

    public void setFlowingGridControllers(FlowingGridControllers flowingGridControllers) {
        this.flowingGridControllers = flowingGridControllers;
    }

    public void setFileChooser(FileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }
}
