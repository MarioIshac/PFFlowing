package me.theeninja.pfflowing;

import com.google.gson.Gson;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import me.theeninja.pfflowing.bluetooth.EFlowConnector;
import me.theeninja.pfflowing.configuration.Configuration;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionDeserializer;
import me.theeninja.pfflowing.flowing.FlowingRegionSerializer;
import me.theeninja.pfflowing.gui.*;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;
import org.apache.commons.lang3.SystemUtils;
import org.hildan.fxgson.FxGson;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EFlow {
    public static void setAsFullscreenToggler(Stage stage) {
        InputStream iconStream = EFlow.class.getResourceAsStream("/EFlowLogo.png");
        Image icon = new Image(iconStream);
        stage.getIcons().add(icon);

        if (stage.getScene() == null)
            throw new IllegalArgumentException("Stage must have associated scene");

        Scene targetScene = stage.getScene();

        if (targetScene.getRoot() == null)
            throw new IllegalArgumentException("Stage's scene must have associated root");

        Node targetNode = targetScene.getRoot();

        targetNode.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            if (keyEvent.getCode() != KeyCode.F11)
                return;

            boolean isFullScreen = stage.isFullScreen();
            stage.setFullScreen(!isFullScreen);
        });
    }

    private final static EFlow INSTANCE = new EFlow();
    private final Gson gson;
    private final Configuration configuration;

    public static final String APPLICATION_NAME = "EFlow";
    public static final String BLOCKS_DIRECTORY = "Blocks";
    public static final String CONFIG_FILE = "config.json";

    private final Map<Boolean, String> OS_DEFAULT_DIRECTORIES = new HashMap<>();

    private void populateDefaultDirectories() {
        OS_DEFAULT_DIRECTORIES.put(SystemUtils.IS_OS_UNIX, System.getProperty("user.home") + "/.config");
        OS_DEFAULT_DIRECTORIES.put(SystemUtils.IS_OS_WINDOWS, System.getenv("AppData"));
    }

    private String getDefault() {
        System.out.println("DE " + OS_DEFAULT_DIRECTORIES);

        for (final Map.Entry<Boolean, String> entry : OS_DEFAULT_DIRECTORIES.entrySet()) {
            final boolean isOperatingSystem = entry.getKey();

            if (entry.getKey()) {
                return entry.getValue();
            }
        }

        throw new IllegalStateException("Operating system not supported");
    }

    private Gson newGson() {
        return FxGson.fullBuilder()
                .excludeFieldsWithoutExposeAnnotation()

                .registerTypeAdapter(FlowingRegion.class, new FlowingRegionSerializer())
                .registerTypeAdapter(FlowingRegion.class, new FlowingRegionDeserializer())

                .registerTypeAdapter(FlowGrid.class, new FlowingGridSerializer())
                .registerTypeAdapter(FlowGrid.class, new FlowingGridDeserializer())

                .registerTypeAdapter(Round.class,         new RoundSerializer())
                .registerTypeAdapter(Round.class,         new RoundDeserializer())

                .setPrettyPrinting()

                .serializeNulls()

                .create();
    }

    private EFlow() {
        populateDefaultDirectories();

        this.gson = newGson();

        handleFiles();

        this.configuration = loadConfiguration();
    }

    public static void main(String[] args) {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        FlowApp.launch(FlowApp.class);
    }

    public static EFlow getInstance() {
        return INSTANCE;
    }


    public Path getFullAppPath() {
        return Paths.get(getDefault(), APPLICATION_NAME);
    }

    public Path getCardsPath() {
        return Paths.get(getDefault(), APPLICATION_NAME, BLOCKS_DIRECTORY);
    }

    public Path getConfigPath() {
        return Paths.get(getDefault(), APPLICATION_NAME, CONFIG_FILE);
    }

    private boolean hasFullAppPath() {
        return Files.exists(getFullAppPath());
    }

    private boolean hasCardsPath() {
        return Files.exists(getCardsPath());
    }

    private boolean hasConfigPath() {
        return Files.exists(getConfigPath());
    }

    private void handleNoFullAppPath() throws IOException {
        Files.createDirectory(getFullAppPath());
    }

    private void handleNoCardsPath() throws IOException {
        Files.createDirectory(getCardsPath());
    }

    private void handleNoConfigPath() throws IOException {
        Files.createFile(getConfigPath());
        Configuration defaultConfiguration = new Configuration();
        String configurationRepresentation = getGSON().toJson(defaultConfiguration, Configuration.class);
        Files.write(getConfigPath(), configurationRepresentation.getBytes());
    }

    private void handleFiles() {
        try {
            if (!hasFullAppPath()) {
                handleNoFullAppPath();
            }
            if (!hasCardsPath()) {
                handleNoCardsPath();
            }
            if (!hasConfigPath()) {
                handleNoConfigPath();
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Configuration loadConfiguration() {
        try {
            byte[] bytes = Files.readAllBytes(getConfigPath());
            String string = new String(bytes);
            return getGSON().fromJson(string, Configuration.class);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private void saveConfiguration() {
        try {
            String json = getGSON().toJson(getConfiguration(), Configuration.class);
            byte[] bytes = json.getBytes();
            Files.write(getConfigPath(), bytes);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Gson getGSON() {
        return gson;
    }

    public static <T> void associateController(Class<T> controllerClass, String resourceLocation, T disassociatedController) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(FlowDisplayController.class.getResource(resourceLocation));
            fxmlLoader.setController(disassociatedController);
            fxmlLoader.load();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
