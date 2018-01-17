package me.theeninja.pfflowing.gui;

import me.theeninja.pfflowing.flowing.DefensiveSpeech;
import me.theeninja.pfflowing.flowing.RefutationSpeech;
import me.theeninja.pfflowing.speech.Side;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class NegationFlowingGridController extends FlowingGridController {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setSpeechList(new SpeechList(Side.NEGATION));

        super.initialize(url, resourceBundle);
        super.addLabels();
    }
}
