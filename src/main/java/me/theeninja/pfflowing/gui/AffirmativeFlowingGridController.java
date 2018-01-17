package me.theeninja.pfflowing.gui;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.utils.Utils;

import java.net.URL;
import java.util.ResourceBundle;

public class AffirmativeFlowingGridController extends FlowingGridController {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setSpeechList(new SpeechList(Side.AFFIRMATIVE));

        super.initialize(url, resourceBundle);
        super.addLabels();
    }
}
