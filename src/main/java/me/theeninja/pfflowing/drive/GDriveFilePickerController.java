package me.theeninja.pfflowing.drive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.drive.google.GDriveConnector;
import netscape.javascript.JSObject;
import org.apache.tika.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class GDriveFilePickerController implements Initializable, SingleViewController<WebView> {
    @FXML
    public WebView drivePicker;

    private void onLoadWorkerStateChanged(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
        JSObject window = (JSObject) getCorrelatingView().getEngine().executeScript("window");
        JavaBridge bridge = new JavaBridge();
        window.setMember("java", bridge);
        getCorrelatingView().getEngine().executeScript("console.log = function(message)\n" +
                "{\n" +
                "    java.log(message);\n" +
                "};");
    }

    private class JavaBridge {
        public void log(String text) {
            System.out.println(text);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        InputStream pickerJSStream = GDriveFilePickerController.class.getResourceAsStream("/gui/drive/drive_picker.html");
        String pickerJS = null;

        try {
            pickerJS = IOUtils.toString(pickerJSStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //getCorrelatingView().getEngine().loadContent(pickerJS);

        try {
            // Build a new authorized API client service.
            Drive service = GDriveConnector.getDriveService();

            // Print the names and IDs for up to 10 files.
            FileList result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files == null || files.size() == 0)
            {
                System.out.println("No files found.");
            }
            else
            {
                System.out.println("Files:");
                for (File file : files)
                {
                    System.out.printf("%s (%s)\n", file.getName(), file.getId());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(pickerJS);

        getCorrelatingView().getEngine().getLoadWorker().stateProperty().addListener(this::onLoadWorkerStateChanged);
    }

    @Override
    public WebView getCorrelatingView() {
        return drivePicker;
    }
}
