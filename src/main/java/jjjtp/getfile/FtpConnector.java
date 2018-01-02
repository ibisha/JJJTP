package jjjtp.getfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jjjtp.beans.FileBean;
import jjjtp.beans.MachineBean;

public class FtpConnector implements ServerConnector {

    private static Logger logger = LogManager.getLogger(FtpConnector.class);

    private MachineBean selected = null;

    private FTPClient ftpClient = new FTPClient();

    public FtpConnector(MachineBean machine) {
        selected = machine;
    }

    @Override
    public boolean getFile(List<FileBean> targetFiles, String savePath) {
        boolean ret = false;
        Path saveDir = Paths.get(savePath);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String now = sdf.format(System.currentTimeMillis());

        for (FileBean target: targetFiles) {
            // 出力ファイルを指定
            File dest = saveDir.resolve(now + "-" + selected.getName() + "-" + target.getFileName().replaceAll("\\\\","_")).toFile();

            try (FileOutputStream out = new FileOutputStream(dest)) {
                logger.info("copy... " + target.getAbsolutePath().toString() + " -> " + savePath);
                // 出力ファイルに書き出す
                ret = ftpClient.retrieveFile(target.getAbsolutePath().toString(), out);
            } catch (IOException e) {
                logger.error(e);
            }

            // ftp転送元のタイムスタンプに修正
            dest.setLastModified(target.getTime());
        }

        return ret;
    }

    @Override
    public List<FileBean> getFileName(FileBean fileTemp) throws IOException {
        // ファイルフィルタ
        FTPFileFilter filter = new FTPFileFilter() {
            Pattern pattern = Pattern.compile(fileTemp.getKeyword());

            @Override
            public boolean accept(FTPFile ftpFile) {
                return (ftpFile.isFile() && pattern.matcher(ftpFile.getName()).find());
            }
        };

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String dir = fileTemp.getDirectory();
        FTPFile[] ftpFiles = ftpClient.listFiles(dir, filter);
        List<FileBean> fileList = new ArrayList<>();

        for (FTPFile ftpFile : ftpFiles) {
            if (ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION)) {
                fileList.add(new FileBean(fileTemp.getType(), dir, dir + ftpFile.getName(),
                        sdf.format(ftpFile.getTimestamp().getTime()), ftpFile.getTimestamp().getTimeInMillis()));
            } else {
                // 権限が無いものはログに表示し、一覧に表示しない
                logger.info("読み取り権限が " + selected.getIp() + "/" + selected.getUser() + " にありません：" + dir + ftpFile.getName());
            }
        }

        return fileList;
    }

    @Override
    public boolean open() {
        try {
            ftpClient.setControlEncoding(selected.getFtpEncoding());
            ftpClient.connect(selected.getIp(), selected.getFtpPort());
            // 正常に接続できたか
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                close();
                throw new IOException("FTP server(" + selected.getName() + "[" + selected.getIp() + "]) refused connection. (" + replyCode + ")");
            }
            // ログイン
            if (!ftpClient.login(selected.getUser(), selected.getPassword())) {
                ftpClient.disconnect();
                throw new IOException("FTP server(" + selected.getName() + "[" + selected.getIp() + "]) refused connection. (" + replyCode + ")");
            }

            // PASVモードに設定
            ftpClient.enterLocalPassiveMode();
            // バイナリ方式に変更
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // バッファサイズを1MBに
            ftpClient.setBufferSize(1024 * 1024);
        } catch (IOException e) {
            logger.error(e);
            return false;
        }

        return false;
    }

    @Override
    public boolean close() {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
            }
            return true;
        } catch (IOException e) {
            logger.error(e);
            return false;
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

}
