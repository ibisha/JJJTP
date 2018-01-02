package jjjtp.beans;

import java.util.stream.Stream;

/**
 * サーバの接続情報を表すBean
 *
 */
public class MachineBean {
    /** サーバ名 */
    private String name;
    /** IP */
    private String ip;
    /** FTPポート(デフォルト:21) */
    private int ftpPort = 21;
    /** ログインユーザ */
    private String user;
    /** ログインパスワード */
    private String password;
    /** ファイル検索時の埋め込み文字 */
    private String embed;
    /** ファイル転送プロトコル */
    private Protocol proto;
    /** ファイルグループ */
    private String group;
    /** FTPサーバのファイル名の文字コード */
    private String ftpEncoding = "SJIS";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmbed() {
        return embed;
    }

    public void setEmbed(String embed) {
        this.embed = embed;
    }

    public Protocol getProto() {
        return proto;
    }

    public void setProto(Protocol proto) {
        this.proto = proto;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getFtpEncoding() {
        return ftpEncoding;
    }

    public void setFtpEncoding(String ftpEncoding) {
        this.ftpEncoding = ftpEncoding;
    }

    public enum Protocol {
        FTP, SMB,
    }

    public enum Server {
        Windows(1), Linux(2);

        private int id;

        public int getId() {
            return id;
        }

        private Server(int id) {
            this.id = id;
        }

        public static Server getTypeById(String strId) {
            try {
                Integer.parseInt(strId);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            }

            Stream<Server> stream = Stream.of(values());
            return stream.filter(s -> Integer.toString(s.getId()).equals(strId)).findAny().get();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public String paramString() {
        return "名前:" + name + ", IP:" + ip + ", FTP PORT:" + ftpPort + ", ユーザ:" + user + ", パスワード:" + password
                + ", プロトコル:" + proto + ", サーバ:" + group;
    }

}
