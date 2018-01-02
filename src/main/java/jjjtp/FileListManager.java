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

    public FileListManager(MachineBean selected, List<FileBean> fileList) {
        this.selected = selected;
        this.fileList = new ArrayList<FileBean>();
        for (FileBean fileBean : fileList) {
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
    public void createFileTable(ObservableList<FileBean> items) {
        List<FileBean> targetFiles = fileList.stream().filter(i -> i.getGroup().equals(selected.getGroup()))
                .collect(Collectors.toList());
        for (FileBean targetFile : targetFiles) {
            try {
                // targetに一致する一覧をサーバから取得する
                List<FileBean> files = serverConnector.getFileName(targetFile);
                items.addAll(files);
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    public boolean downloadFile(List<FileBean> targetFile, String savePath) {
        return serverConnector.getFile(targetFile, savePath);
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
