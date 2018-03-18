package me.theeninja.pfflowing.drive;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.configuration.InternalConfiguration;

import java.net.URL;
import java.text.DateFormat;
import java.util.*;

public class GDriveFilePickerController implements Initializable, SingleViewController<TreeTableView<File>> {
    private final List<File> files;

    public GDriveFilePickerController(List<File> files) {
        this.files = files;
    }

    @FXML
    public TreeTableView<File> picker;

    private ObservableValue<String> getDateCellValueFactory(TreeTableColumn.CellDataFeatures<File, String> param) {
        DateTime dateTime = param.getValue().getValue().getCreatedTime();
        Date date = new Date(dateTime.getValue());
        String strDate = InternalConfiguration.DATE_FORMAT.format(date);

        return new ReadOnlyStringWrapper(strDate);
    }

    private ObservableValue<String> getNameCellValueFactory(TreeTableColumn.CellDataFeatures<File, String> param) {
        System.out.println("a " + param);
        System.out.println("b " + param.getValue());
        System.out.println("c " + param.getValue().getValue());

        String name = param.getValue().getValue().getName();

        return new ReadOnlyStringWrapper(name);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File dummyFile = new File();
        dummyFile.setName("");
        dummyFile.setCreatedTime(new DateTime(1));

        TreeItem<File> root = new TreeItem<>();

        TreeTableColumn<File, String> nameColumn = new TreeTableColumn<>("Name");

        nameColumn.setPrefWidth(150);
        nameColumn.setCellValueFactory(this::getNameCellValueFactory);

        TreeTableColumn<File, String> dateColumn = new TreeTableColumn<>("Date");
        dateColumn.setPrefWidth(190);
        dateColumn.setCellValueFactory(this::getDateCellValueFactory);

        files.stream().map(TreeItem::new).forEach(root.getChildren()::add);

        picker.setRoot(root);
        picker.getColumns().add(nameColumn);
        picker.getColumns().add(dateColumn);

        root.setExpanded(true);
        picker.setShowRoot(false);
    }

    public List<File> getFiles() {
        return files;
    }

    @Override
    public TreeTableView<File> getCorrelatingView() {
        return picker;
    }
}
