package jjjtp.config;

import javafx.scene.control.TreeItem;
import jjjtp.beans.FileBean;
import jjjtp.beans.MachineBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigParser {

    private static Logger logger = LogManager.getLogger(ConfigParser.class);

    // サーバの情報
    private static final String MACHINE_NAME = "name";
    private static final String MACHINE_IP = "ip";
    private static final String MACHINE_FTPPORT = "ftpport";
    private static final String MACHINE_USER = "user";
    private static final String MACHINE_PASSWORD = "password";
    private static final String MACHINE_GROUP = "group";
    private static final String MACHINE_EMBED = "embed";

    // ファイルの情報
    private static final String FILE_TYPE = "type";
    private static final String FILE_PATH = "directory";
    private static final String FILE_KEYWORD = "keyword";
    // フォルダ階層の深さを指定出来るようにしてあるが、直下より深いと探索に時間がかかることに注意
    // このため、デフォルト推奨(depth=1:直下)
    private static final String FILE_DEPTH = "depth";

    // XPath
    private static final XPathFactory xpathFactory = XPathFactory.newInstance();

    /**
     * サーバ一覧のツリーを作成する
     *
     * @param settingFile 設定ファイル(暗号化されている)
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    public static TreeItem<MachineBean> createMachineTree(String settingFile)
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        TreeItem<MachineBean> machineRoot = new TreeItem<>();

        Document document = encryptedXMLReader(settingFile);

        // XPath
        XPath xpath = xpathFactory.newXPath();

        // envsの名前をツリーのルートにセット
        String location = "/getlog/envs";
        Element envs = (Element) xpath.evaluate(location, document, XPathConstants.NODE);

        if (envs == null) {
            throw new XPathExpressionException("サーバ一覧 " + location + "が設定されていません");
        }

        MachineBean root = new MachineBean();
        root.setName("マシン選択");
        machineRoot.setValue(root);

        // env以下のmachine情報を取得
        NodeList envList = envs.getElementsByTagName("env");

        for (int i = 0; i < envList.getLength(); i++) {
            Node env = envList.item(i);
            TreeItem<MachineBean> envRoot = setEnv(env);
            machineRoot.getChildren().add(envRoot);
        }

        return machineRoot;
    }

    /**
     * サーバの情報をセットする
     *
     * @param env
     * @return
     */
    private static TreeItem<MachineBean> setEnv(Node env) {

        // 環境名をセット
        String envName = ((Element) env).getAttribute(MACHINE_NAME);
        MachineBean envMachine = new MachineBean();
        envMachine.setName(envName);
        TreeItem<MachineBean> envRoot = new TreeItem<>(envMachine);

        // env以下の各サーバ情報をセットする
        NodeList machines = env.getChildNodes();
        for (int i = 0; i < machines.getLength(); i++) {
            Node machine = machines.item(i);
            if (!machine.getNodeName().equals("machine")) {
                continue;
            }
            MachineBean m = new MachineBean();
            NodeList elements = machine.getChildNodes();
            for (int j = 0; j < elements.getLength(); j++) {
                Node elem = elements.item(j);
                if (elem.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                switch (elem.getNodeName()) {
                    case (MACHINE_NAME):
                        m.setName(elem.getTextContent());
                        break;
                    case (MACHINE_IP):
                        m.setIp(elem.getTextContent());
                        break;
                    case (MACHINE_USER):
                        m.setUser(elem.getTextContent());
                        break;
                    case (MACHINE_PASSWORD):
                        m.setPassword(elem.getTextContent());
                        break;
                    case (MACHINE_EMBED):
                        m.setEmbed(elem.getTextContent());
                        break;
                    case (MACHINE_FTPPORT):
                        m.setFtpPort(Integer.parseInt(elem.getTextContent()));
                        break;
                    case (MACHINE_GROUP):
                        m.setGroup(elem.getTextContent());
                        break;
                    default:
                }
            }

            envRoot.getChildren().add(new TreeItem<>(m));
        }

        return envRoot;
    }

    /**
     * ファイル種別一覧を取得
     *
     * @param settingFile
     * @return ファイル種別一覧
     */
    public static List<FileBean> createFileList(String settingFile)
            throws SAXException, ParserConfigurationException, XPathExpressionException {
        List<FileBean> fileList = new ArrayList<>();

        Document document = encryptedXMLReader(settingFile);

        XPath xpath = xpathFactory.newXPath();

        // ファイル種別一覧を取得
        String location = "/getlog/fileTypes/group";
        NodeList fileGroups = (NodeList) xpath.evaluate(location, document, XPathConstants.NODESET);

        if (fileGroups == null) {
            throw new XPathExpressionException("ファイル種別一覧 " + location + "が設定されていません");
        }

        for (int groupCount = 0; groupCount < fileGroups.getLength(); groupCount++) {
            // ロググループ単位で実行
            Element fileGroup = (Element) fileGroups.item(groupCount);
            String groupId = fileGroup.getAttribute("id");
            NodeList fileNodeList = fileGroup.getElementsByTagName("fileType");

            for (int fileTypeCount = 0; fileTypeCount < fileNodeList.getLength(); fileTypeCount++) {
                // グループ内のファイル種類を一つずつ加えていく
                Node logNode = fileNodeList.item(fileTypeCount);
                FileBean fileBean = new FileBean();
                fileBean.setGroup(groupId);
                NodeList elements = logNode.getChildNodes();
                for (int i = 0; i < elements.getLength(); i++) {
                    Node elem = elements.item(i);
                    if (elem.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    switch (elem.getNodeName()) {
                        case (FILE_TYPE):
                            fileBean.setType(elem.getTextContent());
                            break;
                        case (FILE_PATH):
                            if (fileBean.getGroup().equals("Windows")) {
                                fileBean.setDirectory(elem.getTextContent().replaceAll("/", "\\\\"));
                            } else {
                                fileBean.setDirectory(elem.getTextContent());
                            }
                            break;
                        case (FILE_KEYWORD):
                            fileBean.setKeyword(elem.getTextContent());
                            break;
                        case (FILE_DEPTH):
                            fileBean.setMaxDepth(Integer.parseInt(elem.getTextContent()));
                            break;
                        default:
                    }
                }
                fileList.add(fileBean);
            }
        }

        return fileList;
    }

    /**
     * 保存先を取得 /getlog/saveFolder/path の文字列を返す
     *
     * @param settingFile 設定ファイル
     * @return 保存先
     */
    public static String getSaveTo(String settingFile)
            throws SAXException, ParserConfigurationException, XPathExpressionException {
        Document document = encryptedXMLReader(settingFile);
        XPath xpath = xpathFactory.newXPath();
        String location = "/getlog/saveFolder/path";
        Element saveTo = (Element) xpath.evaluate(location, document, XPathConstants.NODE);
        if (saveTo == null) {
            throw new XPathExpressionException("保存先 " + location + "が設定されていません");
        }
        return saveTo.getTextContent();

    }

    /**
     * 暗号化されたXMLファイルを読み込む
     *
     * @param encryptFileName 暗号化されたXMLファイル
     * @return XMLのroot
     * @throws IllegalStateException
     */
    private static Document encryptedXMLReader(String encryptFileName) throws IllegalStateException {
        try (FileInputStream fis = new FileInputStream(encryptFileName)) {

            // ファイル読み込み
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();

            return docBuilder.parse(fis);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.error("設定ファイル読み込み失敗", e);
            throw new IllegalStateException(e);
        }

    }
}
