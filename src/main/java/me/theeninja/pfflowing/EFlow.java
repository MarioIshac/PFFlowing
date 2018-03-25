package me.theeninja.pfflowing;

import com.google.gson.Gson;
import me.theeninja.pfflowing.configuration.Configuration;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionAdapter;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.gui.FlowingGridAdapter;
import me.theeninja.pfflowing.gui.RoundAdapter;
import me.theeninja.pfflowing.tournament.Round;
import org.apache.commons.lang3.SystemUtils;
import org.hildan.fxgson.FxGson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class EFlow {
    private final static EFlow instance = new EFlow();
    private final Gson gson;
    private final Configuration configuration;

    public static final String APPLICATION_NAME = "EFlow";
    public static final String BLOCKS_DIRECTORY = "Blocks";
    public static final String CONFIG_FILE = "config.json";

    private String getDefault() {
        Map<Boolean, String> OS_DEFAULT_DIRECTORIES = new HashMap<>() {{
            put(SystemUtils.IS_OS_UNIX, System.getProperty("user.home") + "/.config");
            put(SystemUtils.IS_OS_WINDOWS, System.getenv("AppData"));
        }};

        for (Map.Entry<Boolean, String> entry : OS_DEFAULT_DIRECTORIES.entrySet()) {
            if (entry.getKey())
                return entry.getValue();
        }
        return null;
    }

    private Gson newGson() {
        return FxGson.fullBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(FlowingRegion.class, new FlowingRegionAdapter())
                .registerTypeAdapter(FlowGrid.class,      new FlowingGridAdapter())
                .registerTypeAdapter(Round.class,         new RoundAdapter())

                .setPrettyPrinting()

                .create();
    }

    private EFlow() {
        gson = newGson();

        if (isFirstTime())
            handleFirstTime();

        configuration = loadConfiguration();
    }

    public static void main(String[] args) {
        FlowApp.launch(FlowApp.class);
    }

    public static EFlow getInstance() {
        return instance;
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

    private boolean isFirstTime() {
        return !Files.exists(getFullAppPath());
    }

    private void handleFirstTime() {
        try {
            // EFlow
            Files.createDirectory(getFullAppPath());

            // EFlow/config.json
            Files.createFile(getConfigPath());
            Configuration defaultConfiguration = new Configuration();
            String configurationRepresentation = getGSON().toJson(defaultConfiguration, Configuration.class);
            Files.write(getConfigPath(), configurationRepresentation.getBytes());

            // EFlow/Cards
            Files.createDirectory(getCardsPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Configuration loadConfiguration() {
        try {
            byte[] bytes = Files.readAllBytes(getConfigPath());
            String string = new String(bytes);
            return getGSON().fromJson(string, Configuration.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private void saveConfiguration() {
        try {
            String json = getGSON().toJson(getConfiguration(), Configuration.class);
            byte[] bytes = json.getBytes();
            Files.write(getConfigPath(), bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Gson getGSON() {
        return gson;
    }
}
