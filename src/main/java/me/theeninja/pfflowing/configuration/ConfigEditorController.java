package me.theeninja.pfflowing.configuration;

public class ConfigEditorController {

    private final Configuration configuration;

    ConfigEditorController(Configuration configuration) {
        this.configuration = configuration;
    }

    private void updateConfigWithGUI() {

    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
