package jjjtp;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.ObservableList;
import jjjtp.beans.FileBean;
import jjjtp.beans.MachineBean;
import jjjtp.getfile.FtpConnector;
import jjjtp.getfile.ServerConnector;

public class FileListManager {
    private static Logger logger = LogManager.getLogger(FileListManager.class);

    /** ファイル取得方法 */
    private final ServerConnector serverConnector;
    /** 選択されたサーバ */
    private final MachineBean selected;
    /** 一覧 */
    private List<FileBean> fileList;

    public FileListManager(MachineBean selected, List<FileBean> logFileList) {
        this.selected = selected;
        this.fileList = new ArrayList<FileBean>();
        for (FileBean fileBean : logFileList) {
            try {
                this.fileList.add(fileBean.clone());
            } catch (CloneNotSupportedException e) {
                logger.error(e);
            }
        }

        serverConnector = new FtpConnector(selected);

        // 設定ファイルで埋め込み文字がある場合は、Directoryをフォーマット
        if (selected.getEmbed() == null) {
            this.fileList.forEach(i -> i.setDirectory(i.getDirectory()));
        } else {
            this.fileList.forEach(i -> i.setDirectory(MessageFormat.format(i.getDirectory(), selected.getEmbed())));
        }
    }

    /**
     * 一覧テーブルの作成
     *
     * @param items
     * @throws IOException
     */
    public void createLogTable(ObservableList<FileBean> items) {
        List<FileBean> targetLogs = fileList.stream().filter(i -> i.getGroup().equals(selected.getGroup()))
                .collect(Collectors.toList());
        for (FileBean targetLog : targetLogs) {
            try {
                // targetLogに一致する一覧をサーバから取得する
                List<FileBean> logs = serverConnector.getFileName(targetLog);
                items.addAll(logs);
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    public boolean downloadLog(List<FileBean> targetLogs, String savePath) {
        return serverConnector.getFile(targetLogs, savePath);
    }

    public void cancel() {
        if (serverConnector != null) {
            serverConnector.close();
        }
    }

    public void open() {
        serverConnector.open();
    }
}
