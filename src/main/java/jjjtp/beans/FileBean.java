package jjjtp.beans;

/**
 * ファイル情報を表すBean
 */
public class FileBean implements Cloneable {
    /** グループ */
    private String group;
    /** ファイル種別 */
    private String type;
    /** 検索ディレクトリ */
    private String directory;
    /** ファイル検索条件 デフォルト.*(すべて) */
    private String keyword = ".*";
    /**
     * 再帰的に調べる階層の深さ デフォルト:1 </br>
     * ※直下より深いと時間がかかるので、デフォルト推奨
     */
    private int maxDepth = 1;

    /** ファイルフルパス */
    private String absolutePath;
    /** タイムスタンプ */
    private String timestamp;
    /** タイムスタンプに対するCalendarの時間値（ミリ秒）ftp時のみ使う */
    private Long time;

    public FileBean() {
        this("", "", "", "");
    }

    public FileBean(String type, String directory, String absolutePath, String timestamp) {
        setType(type);
        setDirectory(directory);
        setAbsolutePath(absolutePath);
        setTimestamp(timestamp);
    }

    public FileBean(String type, String directory, String absolutePath, String timestamp, Long time) {
        setType(type);
        setDirectory(directory);
        setAbsolutePath(absolutePath);
        setTimestamp(timestamp);
        setTime(time);
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    public FileBean clone() throws CloneNotSupportedException {
        return (FileBean) super.clone();
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getTime() {
        return time;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    /**
     * フルパスからファイル名のみ抜出す
     *
     * @return
     */
    public String getFileName() {
        if (absolutePath == null)
            return "";
        return absolutePath.replace(directory, "");
    }

    public String paramString() {
        return "グループ:" + group + ", ファイル種別:" + type + ", 検索ディレクトリ:" + directory + ", ファイル検索条件:" + keyword
                + ", 再帰的に調べる階層の深さ:" + maxDepth + ", ファイルフルパス:" + absolutePath + ", タイムスタンプ:" + timestamp;

    }
}
