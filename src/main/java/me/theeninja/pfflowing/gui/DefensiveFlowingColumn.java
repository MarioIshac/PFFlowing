package me.theeninja.pfflowing.gui;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import me.theeninja.pfflowing.Configuration;
import me.theeninja.pfflowing.flowing.Speech;

import java.util.function.Consumer;

public class DefensiveFlowingColumn extends FlowingColumn {
    public DefensiveFlowingColumn(Speech speech) {
        super(speech);
    }

    @Override
    public void addFlowingRegionWriter(boolean createNewOne, Consumer<String> postEnterAction) {
        TextArea textArea = new TextArea();
        textArea.prefWidthProperty().bind(this.prefWidthProperty());
        textArea.setWrapText(true);
        textArea.setFont(Configuration.FONT);

        textArea.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER && keyEvent.isControlDown()) {
                postEnterAction.accept(textArea.getText());

                // After user procedure "postEnterAction" has been finished, reset environment
                getChildren().remove(textArea);
                if (createNewOne)
                    addFlowingRegionWriter(true, postEnterAction);
            }
        });

        this.getChildren().add(textArea);
        textArea.requestFocus();

        FlowingColumnsController.getFXMLInstance().addCardSelectorSupport(textArea);
    }
}
