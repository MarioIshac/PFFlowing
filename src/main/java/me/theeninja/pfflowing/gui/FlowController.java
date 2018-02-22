package me.theeninja.pfflowing.gui;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.theeninja.pfflowing.PFFlowing;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionAdapter;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;
import me.theeninja.pfflowing.tournament.UseType;
import me.theeninja.pfflowing.utils.Utils;
import org.hildan.fxgson.FxGson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FlowController implements Initializable, SingleViewController<FlowingPane> {
    @FXML public FlowingPane pfFlowingMain;
    @FXML public MenuBar navigator;
    @FXML public HBox notificationDisplay;
    @FXML public TabPane roundsBar;

    private UseType useType = UseType.NONE;
    private final ImmutableMap<KeyCodeCombination, Runnable> KEY_CODES = ImmutableMap.of(
        KeyCodeCombinationUtils.TOGGLE_FULLSCREEN, () -> PFFlowing.getInstance().toggleFullscreen(),
        KeyCodeCombinationUtils.SAVE, () -> {
                try {
                    saveSelectedRound();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            },
        KeyCodeCombinationUtils.OPEN, () -> {
                try {
                    openRound();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    );

    @Override
    public FlowingPane getCorrelatingView() {
        return pfFlowingMain;
    }

    private static FlowController fxmlInstance;

    public static FlowController getFXMLInstance() {
        if (fxmlInstance == null) {
            fxmlInstance = Utils.getCorrelatingController("/flow.fxml");
        }
        return fxmlInstance;
    }

    private EventHandler<KeyEvent> getKeyEventHandler() {
        return keyEvent -> {
            for (Map.Entry<KeyCodeCombination, Runnable> entry : KEY_CODES.entrySet())
                if (entry.getKey().match(keyEvent))
                    entry.getValue().run();
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getCorrelatingView().addEventHandler(KeyEvent.KEY_PRESSED, getKeyEventHandler());

        navigator.setPrefHeight(Region.USE_PREF_SIZE);
        roundsBar.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        roundsBar.getSelectionModel().selectedItemProperty().addListener(this::onSelectedTabChanged);

        roundsBar.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (KeyCodeCombinationUtils.SWITCH_SPEECHLIST.match(keyEvent)) {
                RoundTab roundTab = (RoundTab) roundsBar.getSelectionModel().getSelectedItem();
                Round selectedRound = roundTab.getRound();
                selectedRound.setDisplayedSide(selectedRound.getDisplayedSide().getOpposite());
            }
        });

        roundsBar.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue &&
                    // Ensures that we do not attempt to request focus on initialization, where
                    // roundsBar is given focus automatically
                    !roundsBar.getTabs().isEmpty()) {
                roundsBar.getSelectionModel().getSelectedItem().getContent().requestFocus();
            }
            System.out.println("focus received");
        }));

        // Keep file and directory choosers as global instance variables in order to preserve their state
        // through multiple saves and opens
        setFileChooser(new FileChooser());
        setDirectoryChooser(new DirectoryChooser());

        // Keep states of both choosers identical upon closing and opening of the choosers
        Bindings.bindBidirectional(getFileChooser().initialDirectoryProperty(), getDirectoryChooser().initialDirectoryProperty());

        FileChooser.ExtensionFilter eflowExtensionFilter = new FileChooser.ExtensionFilter("EFlow files (*.eflow)", "*.eflow");
        getFileChooser().getExtensionFilters().add(eflowExtensionFilter);

        initializeGSON();
    }

    private static final KeyCodeCombination FINISH = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    private static final String SELECTION_LABEL_CLASS = "selectionLabel";
    private static final String SELECTED_SELECTION_LABEL_CLASS = "selectedSelectionLabel";

    private void saveRoundThroughKey() {
        try {
            saveSelectedRound();
        } catch (IOException e) {
            NotificationDisplayController.getFXMLInstance().error("Unable to save selected round");
            e.printStackTrace();
        }
    }

    public void addRound() {
        Stage configStage = new Stage();
        VBox configLayout = new VBox();
        Scene configScene = new Scene(configLayout);
        configStage.setScene(configScene);

        Label nameLabel = new Label("Name");
        TextField nameField = new TextField();

        CheckBox inTournamentCheckbox = new CheckBox();
        inTournamentCheckbox.setText("New Tournament");

        Label tournamentLabel = new Label("Tournament");
        TextField tournamentField = new TextField();

        HBox tournamentRequest = new HBox(tournamentLabel, tournamentField);
        tournamentRequest.visibleProperty().bind(inTournamentCheckbox.selectedProperty());

        ComboBox<Side> comboBox = new ComboBox<>();
        comboBox.getItems().add(Side.AFFIRMATIVE);
        comboBox.getItems().add(Side.NEGATION);
        comboBox.setPromptText("Side");

        Button finish = new Button("Finish");

        configLayout.getChildren().addAll(
                new HBox(nameLabel, nameField),
                inTournamentCheckbox,
                tournamentRequest,
                comboBox,
                finish
        );

        Runnable options[][] = {
            {
                () -> { // add round to new tournament with something in use

                },

                () -> { // add round to new tournament with nothing in use
                    roundsBar.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
                        RoundTab roundTab = (RoundTab) newValue;
                        roundsBar.getSelectionModel().select(roundTab);
                        System.out.println("new round" + roundTab.getRound().getName());
                    }));

                    addRound(nameField.getText(), comboBox.getValue());
                    configStage.hide();
                }
            },

            {
                () -> { // don't add round to new tournament with something in use
                    addRound(nameField.getText(), comboBox.getValue());
                    configStage.hide();
                },

                () -> { // don't add round to new tournament with nothing in use

                }
            }
        };

        Runnable onSubmission = () -> {
            if (nameField.getText().isEmpty())
                nameField.setPromptText("Round name?");
            else {
                options[inTournamentCheckbox.isSelected() ? 0 : 1]
                        [getUseType().isInUse() ? 0 : 1]
                        .run();
            }
        };

        configLayout.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            if (FINISH.match(keyEvent))
                onSubmission.run();
        });

        finish.addEventHandler(ActionEvent.ACTION, actionEvent ->
                onSubmission.run()
        );

        configStage.show();
    }

    private void onSelectedControllerChanged(ObservableValue<? extends FlowingGridController> observableController, FlowingGridController oldController, FlowingGridController newController) {
        System.out.println("Controller switched for " + newController.getCorrelatingView().getChildren());
        newController.getCorrelatingView().requestFocus();
    }

    private void onSelectedTabChanged(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        System.out.println("Tab changed");

        if (oldValue != null) {
            RoundTab oldRoundTab = (RoundTab) oldValue;
            System.out.println("Listener removed from " + oldRoundTab.getRound().getName());
            oldRoundTab.getRound().selectedControllerProperty().removeListener(this::onSelectedControllerChanged);

        }
        RoundTab newRoundTab = (RoundTab) newValue;
        System.out.println("Listener added to " + newRoundTab.getRound().getName());

        newRoundTab.getRound().selectedControllerProperty().addListener(this::onSelectedControllerChanged);

        newRoundTab.getContent().requestFocus();
    }

    private void addRound(String roundName, Side side) {
        Round round = new Round(side);
        RoundTab roundTab = new RoundTab(round);
        roundTab.textProperty().bind(round.nameProperty());
        round.setName(roundName);
        round.setDisplayedSide(round.getSide());
        roundsBar.getTabs().add(roundTab);
    }

    private void initializeGSON() {
        setGson(
                FxGson.coreBuilder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .registerTypeAdapter(FlowingRegion.class, new FlowingRegionAdapter())
                        .registerTypeAdapter(FlowingGrid.class, new FlowingGridAdapter())
                        .registerTypeAdapter(Round.class, new RoundAdapter())
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
        if (!file.getAbsolutePath().endsWith("." + FILE_EXTENSION))
            return new File(file.getAbsoluteFile() + "." + FILE_EXTENSION);
        else
            return file;
    }

    private static final Type FLOWING_GRIDS_TYPE = new TypeToken<List<FlowingGrid>>(){}.getType();

    public void saveRoundAs() throws IOException {
        getFileChooser().setTitle(SAVE_TITLE);
        File file = getFileChooser().showSaveDialog(PFFlowing.getInstance().getStage());

        // no file chosen
        if (file == null)
            return; // assume that user cancelled saving

        File eflowFile = getEFlowTypeFile(file);

        Path path = eflowFile.toPath();

        RoundTab roundTab = (RoundTab) roundsBar.getSelectionModel().getSelectedItem();
        Round round = roundTab.getRound();

        String json = getGSON().toJson(round, Round.class);
        Files.write(path, json.getBytes());
    }

    public void saveSelectedRound() throws IOException {
        RoundTab roundTab = (RoundTab) roundsBar.getSelectionModel().getSelectedItem();
        Round targetRound = roundTab.getRound();

        String json = getGSON().toJson(targetRound, Round.class);
        Files.write(targetRound.getPath(), json.getBytes());
    }

    private static final String OPEN_ROUND_TITLE = "Open an EFlow Round";
    private static final String OPEN_TOURNAMENT_TITLE = "Open an EFlow Tournament";

    public void openRound() throws IOException {
        getFileChooser().setTitle(OPEN_ROUND_TITLE);
        File file = getFileChooser().showOpenDialog(PFFlowing.getInstance().getStage());

        // no file chosen
        if (file == null)
            return; // assume that user cancelled opening

        File eflowFile = getEFlowTypeFile(file);

        Path path = eflowFile.toPath();

        byte[] jsonBytes = Files.readAllBytes(path);
        String json = new String(jsonBytes);

        Round round = gson.fromJson(json, Round.class);
        round.setPath(path);

        if (getUseType().isInUse()) {

        }
        else {
            RoundTab roundTab = new RoundTab(round);
            roundsBar.tabMinWidthProperty().bind(roundsBar.widthProperty());
            roundsBar.tabMaxWidthProperty().bind(roundsBar.tabMinWidthProperty());
            setUseType(UseType.ROUND);
            roundsBar.getTabs().add(roundTab);
        }
    }

    public void openTournament() throws IOException {
        getDirectoryChooser().setTitle(OPEN_TOURNAMENT_TITLE);
        File directory = getDirectoryChooser().showDialog(PFFlowing.getInstance().getStage());

        // no directory chosen
        if (directory == null)
            return; // assume that user cancelled opening

        Path tournamentPath = directory.toPath();

        List<Path> roundPaths = Files.walk(tournamentPath).filter(Files::isRegularFile).collect(Collectors.toList());

        if (getUseType().isInUse()) {

        }
        else {
            for (Path roundPath : roundPaths) {
                byte[] jsonBytes = Files.readAllBytes(roundPath);
                String json = new String(jsonBytes);

                Round round = gson.fromJson(json, Round.class);
                round.setPath(roundPath);
                RoundTab roundTab = new RoundTab(round);
                roundsBar.getTabs().add(roundTab);
            }
            setUseType(UseType.TOURNAMENT);
        }
    }

    public void newTournament() {
        Stage stage = new Stage();
        VBox prompt = new VBox();
        HBox tournamentRequest = new HBox();
        Scene scene = new Scene(prompt);

        StringProperty directory = new SimpleStringProperty();

        Label tournamentRequestLabel = new Label("Tournament Name:");
        Button directorySelector = new Button("Directory");
        Label directoryViewer = new Label();
        directoryViewer.textProperty().bind(directory);

        directorySelector.setOnAction(actionEvent -> {
            File file = getDirectoryChooser().showDialog(stage);
            directory.set(file.getAbsolutePath());
        });

        TextField tournamentRequestField = new TextField();

        Button finish = new Button("Finish");

        tournamentRequest.getChildren().addAll(tournamentRequestLabel, tournamentRequestField);

        finish.setOnAction(actionEvent -> {
            Path parentDirectoryPath = Paths.get(directory.get());
            String tournamentName = tournamentRequestField.getText();
            Path entireDirectoryPath = parentDirectoryPath.resolve(tournamentName);
            System.out.println(entireDirectoryPath);
            try {
                Files.createDirectory(entireDirectoryPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.hide();

            setUseType(UseType.TOURNAMENT);
        });

        prompt.getChildren().addAll(
                tournamentRequest,
                directorySelector,
                directoryViewer,
                finish
        );

        stage.setScene(scene);
        stage.show();
    }

    public Gson getGSON() {
        return gson;
    }

    public void setFileChooser(FileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;

    public FileChooser getFileChooser() {
        return fileChooser;
    }

    public DirectoryChooser getDirectoryChooser() {
        return directoryChooser;
    }

    public void setDirectoryChooser(DirectoryChooser directoryChooser) {
        this.directoryChooser = directoryChooser;
    }

    public UseType getUseType() {
        return useType;
    }

    public void setUseType(UseType useType) {
        this.useType = useType;
    }
}
