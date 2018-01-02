package jjjtp;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import jjjtp.beans.FileBean;
import jjjtp.beans.MachineBean;
import jjjtp.config.ConfigParser;

public class Controller implements Initializable {

    Logger logger = org.apache.logging.log4j.LogManager.getLogger(Controller.class);

    /** ルート */
    @FXML
    VBox root;

    /** サーバ一覧ツリー */
    @FXML
    TreeView<MachineBean> machineList;

    /** 選択マシン名表示フィールド */
    @FXML
    TextField fieldMachineName;
    /** 保存先表示フィールド */
    @FXML
    TextField fieldSaveTo;

    /** キャンセルボタン */
    @FXML
    Button btnCancel;
    /** ダウンロードボタン */
    @FXML
    Button btnDownload;

    /** ファイル一覧テーブル */
    @FXML
    TableView<FileBean> tableLog;
    @FXML
    TableColumn<FileBean, String> columnFileName;
    @FXML
    TableColumn<FileBean, String> columnTimeStamp;

    /** 選択されているサーバ */
    MachineBean selectedMachine;
    /** ファイル一覧 */
    List<FileBean> logFileList;

    /** ファイル管理 */
    FileListManager fileListManager;

    private static final String SETTING_FILE = "setting.xml";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // サーバ一覧のルートノード
            TreeItem<MachineBean> machineRoot = ConfigParser.createMachineTree(SETTING_FILE);
            machineRoot.setExpanded(true);
            machineList.setRoot(machineRoot);

            // 一覧を取得
            logFileList = ConfigParser.createFileList(SETTING_FILE);

            fieldSaveTo.setText(ConfigParser.getSaveTo(SETTING_FILE));

            columnFileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            columnTimeStamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
            tableLog.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            machineList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                // サーバを選択した時に名前が表示されるよう設定
                selectedMachine = newVal.getValue();
                fieldMachineName.setText(selectedMachine.getName());
            });
        } catch (Exception e) {
            logger.error("JJJTP初期化失敗", e);
            Platform.exit();
        }
    }

    /**
     * サーバのファイル一覧を取得する
     *
     * @param event
     */
    @FXML
    public void doGetLogList(ActionEvent event) {
        if (selectedMachine.getIp() == null || selectedMachine.getIp().length() == 0) {
            return;
        }

        // 取得したいファイルの種類を選択するダイアログ
        ChoiceDialog<FileBean> dialog = new ChoiceDialog<>();
        ListView<FileBean> logList = new ListView<>();
        logList.setItems(FXCollections.observableArrayList());
        logFileList.stream().filter(i -> i.getGroup().equals(selectedMachine.getGroup()))
                .forEach(i -> logList.getItems().add(i));
        MultipleSelectionModel msm = logList.getSelectionModel();
        msm.setSelectionMode(SelectionMode.MULTIPLE);
        dialog.getDialogPane().setContent(logList);
        dialog.getDialogPane().setHeaderText("ファイル選択");

        // OKボタン押下時のみ、選択項目を渡す
        final ObservableList<FileBean>[] selected = new ObservableList[1];
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventHandler(ActionEvent.ACTION, b -> {
            selected[0] = msm.getSelectedItems();
        });
        dialog.showAndWait();

        ObservableList<FileBean> selectedFiles = selected[0];
        if (selectedFiles == null || selectedFiles.size() == 0) {
            return;
        }

        // コントロールの活性化・非活性化
        machineList.setDisable(true);
        btnCancel.setDisable(false);
        btnDownload.setDisable(false);
        // テーブルの生成
        ObservableList<FileBean> tableItems = FXCollections.observableArrayList();
        tableLog.setItems(tableItems);
        fileListManager = new FileListManager(selectedMachine, selectedFiles);

        fileListManager.open();
        try {
            fileListManager.createLogTable(tableItems);
            if (tableItems.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("INFO");
                alert.setHeaderText("ファイルが見つかりませんでした。");
                alert.show();
            }
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("ERROR");
            alert.setHeaderText("一覧の取得に失敗しました。");
            alert.setContentText("※エラーログ参照");
            alert.show();

            logger.error(e);
            fileListManager.cancel();
            return;
        }
    }

    /**
     * ダウンロードボタン押下時の挙動
     *
     * @param event
     */
    @FXML
    public void doDownload(ActionEvent event) {
        ObservableList<FileBean> selectedItems = tableLog.getSelectionModel().getSelectedItems();
        for (FileBean selectedItem : selectedItems) {
            logger.debug(selectedItem.paramString());
        }
        if (!fileListManager.downloadLog(selectedItems, fieldSaveTo.getText())) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("ダウンロードに失敗しました。");
            alert.setContentText("※エラーログ参照");
            alert.show();
        }
    }

    /**
     * キャンセルボタン押下時の挙動
     *
     * @param event
     */
    @FXML
    public void doCancel(ActionEvent event) {
        // コントロールの活性化・非活性化
        btnDownload.setDisable(true);
        btnCancel.setDisable(true);
        machineList.setDisable(false);
        tableLog.setItems(null);
        if (fileListManager != null) {
            fileListManager.cancel();
        }
    }

    /**
     * 終了ボタン
     *
     * @param event
     */
    @FXML
    public void doClose(ActionEvent event) {
        if (fileListManager != null) {
            fileListManager.cancel();
        }
        Platform.exit();
    }

}
