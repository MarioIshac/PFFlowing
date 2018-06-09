package me.theeninja.pfflowing.gui;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowingregions.Blocks;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;
import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.StyleClassDecoration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controller responsible for managing the interface in which a user can create a block file.
 *
 * @author TheeNinja
 */
public class BlocksCreatorController implements SingleViewController<VBox>, Initializable {
    private final Consumer<Blocks> blocksConsumer;

    @FXML public TextField blockFileRequest;
    @FXML public ComboBox<Side> sideChooser;
    @FXML public VBox root;
    @FXML public Button finishButton;

    private BooleanProperty invalidName = new SimpleBooleanProperty(false);

    private static List<String> getExistingBlockNames() {
        Path blocksDirectory = EFlow.getInstance().getCardsPath();

        try {
            Stream<Path> blocksPaths = Files.walk(blocksDirectory)
                    .filter(path -> Utils.hasExtension(path.toString(), "json"));

            return blocksPaths
                    .map(Utils::readAsString)
                    .map(json -> EFlow.getInstance().getGSON().fromJson(json, Blocks.class))
                    .map(Blocks::getName)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sideChooser.getItems().setAll(Side.values());

        final Decoration invalidNameDecoration = new StyleClassDecoration("invalidName");
        final List<String> blocksNames = getExistingBlockNames();

        blockFileRequest.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                setInvalidName(true);
                return;
            }

            String noCaseName = newValue.toLowerCase();

            boolean doesNameExist = blocksNames.contains(noCaseName);

            setInvalidName(doesNameExist);
        });

        ObservableBooleanValue hasSide = new BooleanBinding() {
            {
                super.bind(sideChooser.valueProperty());
            }

            @Override
            protected boolean computeValue() {
                return sideChooser.getValue() != null;
            }
        };

        finishButton.disableProperty().bind(invalidNameProperty().and(hasSide));
    }

    @Override
    public VBox getCorrelatingView() {
        return root;
    }

    BlocksCreatorController(Consumer<Blocks> blocksConsumer) {
        this.blocksConsumer = blocksConsumer;
    }

    /**
     * Called when the finish button is pressed.
     *
     * @param actionEvent
     */
    @FXML
    public void finish(ActionEvent actionEvent) {
        String blockFileName = blockFileRequest.getText();
        Side blockFileSide = sideChooser.getValue();

        Blocks blocks = new Blocks(blockFileName, blockFileSide);

        getBlocksConsumer().accept(blocks);
    }

    public Consumer<Blocks> getBlocksConsumer() {
        return blocksConsumer;
    }

    public boolean isInvalidName() {
        return invalidName.get();
    }

    public BooleanProperty invalidNameProperty() {
        return invalidName;
    }

    public void setInvalidName(boolean invalidName) {
        this.invalidName.set(invalidName);
    }
}
