package me.theeninja.pfflowing.gui;

import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.theeninja.pfflowing.ActionManager;
import me.theeninja.pfflowing.FlowApp;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.configuration.InternalConfiguration;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;
import me.theeninja.pfflowing.tournament.UseType;
import me.theeninja.pfflowing.utils.Pair;
import me.theeninja.pfflowing.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FlowController implements Initializable, SingleViewController<FlowingPane> {
    @FXML public FlowingPane pfFlowingMain;
    @FXML public HBox notificationDisplay;
    @FXML public TabPane roundsBar;

    private CardSelectorController cardSelectorController;
    private NavigatorController navigatorController;
    private final FlowApp flowApp;

    public FlowController(FlowApp flowApp) {
        this.flowApp = flowApp;
    }

    private ObjectProperty<UseType> useType = new SimpleObjectProperty<>(UseType.NONE);
    private final ImmutableMap<KeyCodeCombination, Runnable> GLOBAL_KEY_CODES = ImmutableMap.of(
        KeyCodeCombinationUtils.TOGGLE_FULLSCREEN, () -> getPFFlowing().toggleFullscreen()
        /*KeyCodeCombinationUtils.SAVE, () -> {
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
            },
        KeyCodeCombinationUtils.SWITCH_SPEECHLIST, () -> {
                RoundTab roundTab = (RoundTab) roundsBar.getSelectionModel().getSelectedItem();
                Round selectedRound = roundTab.getRound();
                selectedRound.setDisplayedSide(selectedRound.getDisplayedSide().getOpposite());
            }*/
    );

    @Override
    public FlowingPane getCorrelatingView() {
        return pfFlowingMain;
    }

    private EventHandler<KeyEvent> getKeyEventHandler() {
        return keyEvent -> {
            System.out.println("Key received + " + keyEvent.toString());
            GLOBAL_KEY_CODES.forEach((key, value) -> {
                if (key.match(keyEvent))
                    value.run();
            });

            if (!roundsBar.getTabs().isEmpty()) {
                RoundTab selectedRoundTab = (RoundTab) roundsBar.getSelectionModel().getSelectedItem();
                Round selectedRound = selectedRoundTab.getRound();

                FlowDisplayController currentVisibleController = selectedRound.getSelectedController();

                KeyEventProcessor keyEventProcessor = new KeyEventProcessor(this, keyEvent);
                keyEventProcessor.process();
            }
        };
    }

    public void switchSpeechList() {
        Round selectedRound = getSelectedRound();
        selectedRound.setDisplayedSide(selectedRound.getDisplayedSide().getOpposite());
        roundsBar.getSelectionModel().getSelectedItem().setContent(selectedRound.getSelectedController().flowDisplay);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roundsBar.addEventHandler(KeyEvent.KEY_PRESSED, getKeyEventHandler());

        // Keep file and directory choosers as global instance variables in order to preserve their state
        // through multiple saves and opens
        setFileChooser(new FileChooser());
        setDirectoryChooser(new DirectoryChooser());

        // Keep states of both choosers identical upon closing and opening of the choosers
        Bindings.bindBidirectional(getFileChooser().initialDirectoryProperty(), getDirectoryChooser().initialDirectoryProperty());

        FileChooser.ExtensionFilter eflowExtensionFilter = new FileChooser.ExtensionFilter("EFlow files (*.eflow)", "*.eflow");
        getFileChooser().getExtensionFilters().add(eflowExtensionFilter);

        CardSelectorController cardSelectorController = new CardSelectorController(getPFFlowing());
        setUpController(cardSelectorController, "/card_parser_gui/card_selector.fxml", this::setCardSelectorController);
        getCorrelatingView().setLeft(cardSelectorController.getCorrelatingView());

        NavigatorController navigatorController = new NavigatorController(getPFFlowing());
        setUpController(navigatorController, "/navigator.fxml", this::setNavigatorController);
        getCorrelatingView().setTop(navigatorController.getCorrelatingView());

        getNavigatorController().getCorrelatingView().setPrefHeight(Region.USE_PREF_SIZE);
        roundsBar.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        useTypeProperty().addListener(this::onUseTypeChanged);
    }

    private <T> void setUpController(T controllerInstance, String fileName, Consumer<T> setter) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fileName));
            fxmlLoader.setController(controllerInstance);
            fxmlLoader.load();
            setter.accept(controllerInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final KeyCodeCombination FINISH = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    private static final String SELECTION_LABEL_CLASS = "selectionLabel";
    private static final String SELECTED_SELECTION_LABEL_CLASS = "selectedSelectionLabel";

    private static final int PIXELS_TO_REMOVE_DROPDOWN = 30;

    public void promptRoundAddition() {
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
                    FlowApp newApplication = new FlowApp();
                    Stage allocatedStage = new Stage();
                    newApplication.start(allocatedStage);
                    newApplication.getFlowController().addRound(nameField.getText(), comboBox.getValue());

                    setUseType(UseType.TOURNAMENT);
                },

                () -> { // add round to new tournament with nothing in use
                    this.addRound(nameField.getText(), comboBox.getValue());

                    setUseType(UseType.TOURNAMENT);
                }
            },

            {
                () -> { // don't add round to new tournament with something in use
                    FlowApp newApplication = new FlowApp();
                    Stage allocatedStage = new Stage();
                    newApplication.start(allocatedStage);
                    newApplication.getFlowController().addRound(nameField.getText(), comboBox.getValue());

                    setUseType(UseType.ROUND);
                },

                () -> { // don't add round to new tournament with nothing in use
                    this.addRound(nameField.getText(), comboBox.getValue());

                    setUseType(UseType.ROUND);
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
                configStage.hide();
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

    private void addRound(String roundName, Side side) {
        Round round = new Round(side);
        RoundTab roundTab = new RoundTab(round);

        round.setName(roundName);
        round.setDisplayedSide(side);

        roundsBar.getTabs().add(roundTab);

        round.getAffController().flowDisplay.prefWidthProperty().bind(roundsBar.widthProperty());
        round.getNegController().flowDisplay.prefWidthProperty().bind(roundsBar.widthProperty());
    }


    private static final String FILE_EXTENSION = "eflow";
    private static final String SAVE_TITLE = "Save an EFlow";

    private File getEFlowTypeFile(File file) {
        if (!file.getAbsolutePath().endsWith("." + FILE_EXTENSION))
            return new File(file.getAbsoluteFile() + "." + FILE_EXTENSION);
        else
            return file;
    }

    public void saveRoundAs() throws IOException {
        getFileChooser().setTitle(SAVE_TITLE);
        File file = getFileChooser().showSaveDialog(getPFFlowing().getStage());

        // no file chosen
        if (file == null)
            return; // assume that user cancelled saving

        File eflowFile = getEFlowTypeFile(file);

        Path path = eflowFile.toPath();

        RoundTab roundTab = (RoundTab) roundsBar.getSelectionModel().getSelectedItem();
        Round round = roundTab.getRound();

        String json = EFlow.getInstance().getGSON().toJson(round, Round.class);
        Files.write(path, json.getBytes());
    }

    public void saveSelectedRound() throws IOException {
        RoundTab roundTab = (RoundTab) roundsBar.getSelectionModel().getSelectedItem();
        Round targetRound = roundTab.getRound();

        String json = EFlow.getInstance().getGSON().toJson(targetRound, Round.class);
        Files.write(targetRound.getPath(), json.getBytes());
    }

    private static final String OPEN_ROUND_TITLE = "Open an EFlow Round";
    private static final String OPEN_TOURNAMENT_TITLE = "Open an EFlow Tournament";

    public void openRound() throws IOException {
        getFileChooser().setTitle(OPEN_ROUND_TITLE);
        File file = getFileChooser().showOpenDialog(getPFFlowing().getStage());

        // no file chosen
        if (file == null)
            return; // assume that user cancelled opening

        File eflowFile = getEFlowTypeFile(file);

        Path path = eflowFile.toPath();

        byte[] jsonBytes = Files.readAllBytes(path);
        String json = new String(jsonBytes);

        Round round = EFlow.getInstance().getGSON().fromJson(json, Round.class);
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
        File directory = getDirectoryChooser().showDialog(getPFFlowing().getStage());

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

                Round round = EFlow.getInstance().getGSON().fromJson(json, Round.class);
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
            System.out.println("Directory" + entireDirectoryPath);
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
        return useType.get();
    }

    public ObjectProperty<UseType> useTypeProperty() {
        return useType;
    }

    public void setUseType(UseType useType) {
        this.useType.set(useType);
    }

    public FlowApp getPFFlowing() {
        return flowApp;
    }

    public void info(String text) {
        notify(text, Level.INFO);
    }

    public void warn(String text) {
        notify(text, Level.WARNING);
    }

    public void error(String text) {
        notify(text, Level.SEVERE);
    }

    private void notify(String text, Level level) {
        Label notificationLabel = new Label(text);
        notificationLabel.prefWidthProperty().bind(getCorrelatingView().widthProperty());

        Pair<Color, Color> colorPair = InternalConfiguration.LEVEL_COLORS.get(level);
        notificationLabel.setTextFill(colorPair.getFirst());
        notificationLabel.setBackground(Utils.generateBackgroundOfColor(colorPair.getSecond()));

        resetNotification();
        notificationDisplay.getChildren().add(notificationLabel);
    }

    private void resetNotification() {
        notificationDisplay.getChildren().clear();
    }

    public CardSelectorController getCardSelectorController() {
        return cardSelectorController;
    }

    public void setCardSelectorController(CardSelectorController cardSelectorController) {
        this.cardSelectorController = cardSelectorController;
    }

    public void setNavigatorController(NavigatorController navigatorController) {
        this.navigatorController = navigatorController;
    }

    public NavigatorController getNavigatorController() {
        return navigatorController;
    }

    private void onUseTypeChanged(ObservableValue<? extends UseType> observable, UseType oldValue, UseType newValue) {
        switch (newValue) {
            case ROUND: {
                roundsBar.tabMinWidthProperty().unbind();
                roundsBar.tabMinWidthProperty().bind(roundsBar.widthProperty().subtract(PIXELS_TO_REMOVE_DROPDOWN));
                break;
            }
            case TOURNAMENT: {
                roundsBar.tabMinWidthProperty().unbind();
                break;
            }
            case NONE: {
                break;
            }
        }
    }

    public Round getSelectedRound() {
        RoundTab roundTab = (RoundTab) roundsBar.getSelectionModel().getSelectedItem();
        return roundTab.getRound();
    }
}
