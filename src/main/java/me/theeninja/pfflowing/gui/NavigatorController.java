package me.theeninja.pfflowing.gui;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Popup;
import javafx.stage.Stage;
import me.theeninja.pfflowing.PFFlowing;
import me.theeninja.pfflowing.gui.cardparser.CardParserController;
import me.theeninja.pfflowing.utils.Utils;

import java.net.URL;
import java.util.ResourceBundle;

public class NavigatorController implements Initializable {
    private static NavigatorController fxmlInstance;

    private boolean doesFlowFileExist;

    public static NavigatorController getFxmlInstance() {
        return fxmlInstance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxmlInstance = this;

        setDoesFlowFileExist(false);
    }

    public void newFlow(ActionEvent actionEvent) {

    }

    public void openFlow(ActionEvent actionEvent) {

    }

    public void saveFlow() {

    }

    public void saveFlowAs(ActionEvent actionEvent) {

    }

    public void printFlow(ActionEvent actionEvent) {

    }

    public void emailFlow(ActionEvent actionEvent) {

    }

    public boolean doesFlowFileExist() {
        return doesFlowFileExist;
    }

    public void setDoesFlowFileExist(boolean doesFlowFileExist) {
        this.doesFlowFileExist = doesFlowFileExist;
    }

    public void undo(ActionEvent actionEvent) {

    }

    public void redo(ActionEvent actionEvent) {

    }

    public void selectAll(ActionEvent actionEvent) {

    }

    public void openParserPopup() {
        CardParserController cardParserController = Utils.getCorrelatingController("/card_parser.fxml");
        Stage stage = new Stage();
        Scene scene = new Scene(cardParserController.getCorrelatingView());
        cardParserController.startParseProcess("<p>A2 Unfair to Regular Students</p>\n" +
                "<ul>\t<li><p><b>Golman finds that other students don’t produce revenue for the college. At that point, it is not unfair.</b></p>\n" +
                "</li>\n" +
                "\t<li><p><b>Students are given so many opportunities to get employed on campus. They can be tour guides, work in the library, tutor, work in the school store, or even work outside of school.</b></p>\n" +
                "</li>\n" +
                "\t<li><p><b>Student athletes and the rest of the student body are fundamentally different because student athletes spend so much time on sports that it would be unfair not to pay them for their labor.</b></p>\n" +
                "</li>\n" +
                "</ul>\n" +
                "<p />\n" +
                "<p>Other students don’t produce revenue for the college, that’s a fair distinction Lee Goldman [Associate Professor of Law, University of Detroit]. “Sports and Antitrust: Should College Students Be Paid to Play,” Notre Dame Law Review, 1990. </p>\n" +
                "<p><a href=\"http://heinonline.org/HOL/Page?handle=hein.journals/tndl65&amp;div=16&amp;g_sent=1&amp;casa_token=&amp;collection=journals\"><u>http://heinonline.org/HOL/Page?handle=hein.journals/tndl65&amp;div=16&amp;g_sent=1&amp;casa_token=&amp;collection=journals</a></u></p>\n" +
                "<p />\n" +
                "<p> Several colleagues have objected that payments to student-athletes cannot be justified if academic superstars are not similarly rewarded. Not only is the treatment inequitable, but it makes a troubling statement about our society’s priorities. There is no inequity in treatment between superstar athletes and academicians. Each is rewarded according to their value in the free market system.<b><u> Superstar athletes attract huge revenues to the university. Exceptional students, as a rule, do not.</u></b> It is sad that society seems to value athletes more than academicians. This author would certainly prefer Bo Jackson’s salary to his own. Nevertheless, our capitalist system does not provide rankings of occupations’ intrinsic worth. Many might wish to reward special education teachers or social workers more than Sylvester Stallone or Brian Bosworth. Those judgments, however, are left to the operation of the free market. Student-athletes should not be treated disparately. </p>\n" +
                "<p />");
        stage.setScene(scene);
        stage.show();
        cardParserController.setAssociatedStage(stage);
    }
}
