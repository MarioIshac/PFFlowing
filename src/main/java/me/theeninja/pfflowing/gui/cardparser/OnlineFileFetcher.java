package me.theeninja.pfflowing.gui.cardparser;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.SingleViewController;
import me.theeninja.pfflowing.gui.NavigatorController;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class OnlineFileFetcher<T> extends FileFetcher<T> implements Initializable, SingleViewController<TreeTableView<T>> {
    private final Function<T, String> nameProvider;
    private final Function<T, Date> dateProvider;
    @FXML public TreeTableView<T> drivePicker;
    @FXML public TreeTableColumn<T, String> nameColumn;
    @FXML public TreeTableColumn<T, Date> dateColumn;

    private final Stage stage = new Stage();
    private Scene scene;

    OnlineFileFetcher(Function<T, String> nameProvider, Function<T, Date> dateProvider) {
        this.nameProvider = nameProvider;
        this.dateProvider = dateProvider;

        FXMLLoader fxmlLoader = new FXMLLoader(NavigatorController.class.getResource("/gui/drive/drive_picker.fxml"));
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObservableValue<Date> getDateCellValueFactory(TreeTableColumn.CellDataFeatures<T, Date> param) {
        TreeItem<T> treeItem = param.getValue();
        T file = treeItem.getValue();
        Date date = getDateProvider().apply(file);

        return new ReadOnlyObjectWrapper<>(date);
    }

    private ObservableValue<String> getNameCellValueFactory(TreeTableColumn.CellDataFeatures<T, String> param) {
        TreeItem<T> treeItem = param.getValue();
        T file = treeItem.getValue();
        String name = getNameProvider().apply(file);

        return new ReadOnlyStringWrapper(name);
    }

    @Override
    public TreeTableView<T> getCorrelatingView() {
        return drivePicker;
    }

    protected abstract T newDummyFile();
    protected abstract void setUpConnection() throws IOException;
    protected abstract List<T> getPossibleFiles() throws IOException;

    @Override
    protected void feedFetchedFile(Consumer<T> fileConsumerCallback) throws IOException {
        setUpConnection();

        List<T> files = getPossibleFiles();

        if (files == null || files.size() == 0)
            return;

        files.stream().map(TreeItem::new).forEach(getCorrelatingView().getRoot().getChildren()::add);

        getStage().show();
        getCorrelatingView().requestFocus();
        getCorrelatingView().addEventHandler(MouseEvent.MOUSE_CLICKED, onFileSelected(fileConsumerCallback));
    }

    private EventHandler<MouseEvent> onFileSelected(Consumer<T> fileConsumerCallback) {
        return mouseEvent -> {
            // Indicates that the user did not double click the file, hence we don't process the event
            if (mouseEvent.getClickCount() != 2)
                return;

            TreeItem<T> selectedTreeItem = getCorrelatingView().getSelectionModel().getSelectedItem();
            T selectedFile = selectedTreeItem.getValue();

            fileConsumerCallback.accept(selectedFile);

            getStage().hide();
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scene = new Scene(getCorrelatingView());
        getStage().setScene(getScene());

        EFlow.setAsFullscreenToggler(getStage());

        nameColumn.setCellValueFactory(this::getNameCellValueFactory);
        dateColumn.setCellValueFactory(this::getDateCellValueFactory);

        T dummyRootFile = newDummyFile();

        TreeItem<T> dummyRoot = new TreeItem<>(dummyRootFile);

        getCorrelatingView().setRoot(dummyRoot);
    }

    public Function<T, String> getNameProvider() {
        return nameProvider;
    }

    public Function<T, Date> getDateProvider() {
        return dateProvider;
    }

    public Scene getScene() {
        return scene;
    }

    public Stage getStage() {
        return stage;
    }
}
