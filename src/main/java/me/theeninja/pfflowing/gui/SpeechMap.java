package me.theeninja.pfflowing.gui;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import me.theeninja.pfflowing.Configuration;
import me.theeninja.pfflowing.Utils;
import me.theeninja.pfflowing.flowing.Speech;
import me.theeninja.pfflowing.utils.Pair;
import sun.security.krb5.Config;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class SpeechMap extends LinkedHashMap<Speech, Pair<VBox, Pair<Label, VBox>>> {
    public SpeechMap() {
        for (Speech speech : Speech.SPEECH_ORDER) {
            Label representingLabel = new Label(speech.name().replace("_", " ").toLowerCase());
            VBox content = new VBox();

            this.put(speech, new Pair<>(
                new VBox(), new Pair<>(representingLabel, content)));
        }

        initiateColumnComponents();
        styleColumnComponents();
        sizeColumnComponents();
        display();
    }

    private void styleColumnComponents() {
        for (Pair<VBox, Pair<Label, VBox>> pair : values()) {
            // pair.getFirst().setBackground(Utils.generateBackgroundOfColor(Color.LIGHTGRAY));
        }
    }

    private void initiateColumnComponents() {
        for (Pair<VBox, Pair<Label, VBox>> pair : values()) {
            pair.getFirst().getChildren().addAll(pair.getSecond().getFirst(), pair.getSecond().getSecond());
        }
    }

    private void sizeColumnComponents() {
        double calculatedWidth = FlowingColumnsController.getFXMLInstance().calculateSpeechWidth();
        for (Pair<VBox, Pair<Label, VBox>> pair : values()) {
            HBox.setHgrow(pair.getFirst(), Priority.ALWAYS);
            pair.getFirst().setPrefWidth(calculatedWidth);
        }
    }

    private void display() {
        for (Entry<Speech, Pair<VBox, Pair<Label, VBox>>> entry : entrySet()) {
            Pair<VBox, Pair<Label, VBox>>  pair = entry.getValue();
            FlowingColumnsController.getFXMLInstance().getCorrelatingView().getChildren().add(pair.getFirst());
        }
    }

    public VBox getColumnOfSpeech(Speech speech) {
        return get(speech).getFirst();
    }

    public Label getLabelOfSpeech(Speech speech) {
        return get(speech).getSecond().getFirst();
    }

    public VBox getContentContainerOfSpeech(Speech speech) {
        return get(speech).getSecond().getSecond();
    }


    public Speech getSpeechOfContentContainer(VBox contentContainer) {
        for (Entry<Speech, Pair<VBox, Pair<Label, VBox>>> entry : entrySet()) {
            if (entry.getValue().getSecond().getSecond() == contentContainer)
                return entry.getKey();
        }
        return null;
    }

    public Speech getSpeechOfColumn(VBox column) {
        for (Entry<Speech, Pair<VBox, Pair<Label, VBox>>> entry : entrySet()) {
            if (entry.getValue().getFirst() == column)
                return entry.getKey();
        }
        return null;
    }



    public Speech getSpeechOfLabel(Label label) {
        for (Entry<Speech, Pair<VBox, Pair<Label, VBox>>> entry : entrySet()) {
            if (entry.getValue().getSecond().getFirst() == label)
                return entry.getKey();
        }
        return null;
    }
}
