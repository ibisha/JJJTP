package jjjtp.getfile;

import java.io.IOException;
import java.util.List;

import jjjtp.beans.FileBean;
import jjjtp.beans.MachineBean;

public interface ServerConnector {

    /**
     * 選択したファイルを取得する
     *
     * @return
     */
    boolean getFile(List<FileBean> targetFiles, String savePath);

    /**
     * ファイル名の一覧を取得する<br>
     * {@link MachineBean#ip}の{@link FileBean#path}に設定されたパスからファイルを検索する<br>
     * 検索結果は1つずつ{@link FileBean} にセットしてList にして返す
     *
     * @param file ファイル情報
     * @return 検索結果のファイル一覧
     * @throws IOException
     */
    List<FileBean> getFileName(FileBean file) throws IOException;

    /**
     * サーバに接続する
     *
     * @return
     */
    boolean open();

    /**
     * サーバを切断する
     *
     * @return
     */
    boolean close();

}
