package me.theeninja.pfflowing.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.utils.Utils;

import java.net.URL;
import java.util.ResourceBundle;

public class QuestionDetailerController implements Detailer, Initializable {
    @FXML
    public Text questionText;

    private final StringProperty questionMessage;

    public QuestionDetailerController(String question) {
        questionMessage = new SimpleStringProperty(question);
    }

    public String getQuestionMessage() {
        return questionMessage.get();
    }

    public StringProperty questionMessageProperty() {
        return questionMessage;
    }

    @Override
    public boolean hasDetail() {
        return !getQuestionMessage().isEmpty();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        questionText.textProperty().bind(questionMessageProperty());
    }
}
