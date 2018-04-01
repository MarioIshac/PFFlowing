package me.theeninja.pfflowing.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowingregions.Blocks;
import me.theeninja.pfflowing.speech.Side;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class BlockPrompterController implements SingleViewController<VBox>, Initializable {
    private final Consumer<Blocks> blocksConsumer;
    @FXML
    public TextField blockFileRequest;

    @FXML
    public ComboBox<Side> sideChooser;

    @FXML
    public VBox root;

    @FXML
    public Button finishButton;

    BlockPrompterController(Consumer<Blocks> blocksConsumer) {
        this.blocksConsumer = blocksConsumer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sideChooser.getItems().setAll(Side.values());
    }

    @Override
    public VBox getCorrelatingView() {
        return root;
    }

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
}
