package me.theeninja.pfflowing.gui;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import me.theeninja.pfflowing.PFFlowing;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.FlowingRegionAdapter;
import me.theeninja.pfflowing.utils.Utils;
import org.hildan.fxgson.FxGson;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class FlowController implements Initializable, SingleViewController<FlowingPane> {
    @FXML public FlowingPane pfFlowingMain;
    @FXML public MenuBar navigator;
    private FilesBarController filesBarController;
    @FXML public HBox notificationDisplay;

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


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
