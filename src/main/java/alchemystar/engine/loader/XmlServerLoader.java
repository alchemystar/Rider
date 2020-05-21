package alchemystar.engine.loader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author lizhuyang
 */
public class XmlServerLoader implements ServerLoader {

    private static final Logger logger = LoggerFactory.getLogger(XmlServerLoader.class);
    // private String databaseXmlPath = "/Users/alchemystar/tmp/database.xml";
    private String databaseXmlPath = "/database.xml";
    private List<String> schemas;
    private Map<String, List<TableConfig>> tableConfigs;
    private String userName;
    private String passwd;
    private int serverport;
    private static Document doc;

    public XmlServerLoader() {
        schemas = new ArrayList<String>();
        tableConfigs = new HashMap<String, List<TableConfig>>();
    }

    public void init() {
        try {
            // File xml = new File(databaseXmlPath);
            InputStream xml = XmlServerLoader.class.getResourceAsStream(databaseXmlPath);
            SAXReader reader = new SAXReader();
            Document document = reader.read(xml);
            Element root = document.getRootElement();
            loadUser(root);
            loadPort(root);
            loadPasswd(root);
            loadSchema(root);
        } catch (Exception e) {
            System.out.println("Load config error" + e.getMessage());
            System.exit(-1);
        }
    }

    public void loadPort(Element root) {
        List<Element> portElements = root.elements("port");
        if (portElements.isEmpty()) {
            throw new RuntimeException("you must specify the port");
        }
        this.serverport = Integer.valueOf((String) portElements.get(0).getData());
    }

    public void loadUser(Element root) {
        Element userElement = root.element("user");
        if (userElement == null) {
            throw new RuntimeException("you must specify the user");
        }
        this.userName = (String) userElement.getData();
    }

    public void loadPasswd(Element root) {
        Element passElement = root.element("pass");
        if (passElement == null) {
            throw new RuntimeException("you must specify the pass");
        }
        this.passwd = (String) passElement.getData();
    }

    public void loadSchema(Element root) {
        List<Element> schemaElements = root.elements("schema");
        if (schemaElements.isEmpty()) {
            return;
        }
        for (Element temp : schemaElements) {
            Element element = temp.element("name");
            String schema = (String) element.getData();
            List<TableConfig> tableConfigList = new ArrayList<TableConfig>();
            List<Element> tables = temp.elements("table");
            for (Element item : tables) {
                // 创建表的sql
                tableConfigList.add(getTableConfig(item));
            }
            schemas.add(schema);
            tableConfigs.put(schema, tableConfigList);
        }
    }

    private TableConfig getTableConfig(Element elem) {
        TableConfig tableConfig = new TableConfig();
        tableConfig.setSql((String) getElementData(elem, "sql"));
        Element skipElem = elem.element("skipRows");
        if (skipElem != null) {
            tableConfig.setSkipRows(Integer.valueOf((String) skipElem.getData()));
        }
        Element pathElem = elem.element("pathPattern");
        if (pathElem != null) {
            tableConfig.setPath((String) pathElem.getData());
        }
        Element viewSqlElem = elem.element("viewSql");
        if (viewSqlElem != null) {
            tableConfig.setViewSql((String) viewSqlElem.getData());
        }
        Element sheetNumberElem = elem.element("sheetNumber");
        if (sheetNumberElem != null) {
            tableConfig.setSheetNumber(Integer.valueOf((String) sheetNumberElem.getData()));
        }
        return tableConfig;
    }

    private Object getElementData(Element elem, String name) {
        Element subElem = elem.element(name);
        if (subElem == null) {
            throw new RuntimeException("You must specify the element:" + name);
        }
        return subElem.getData();
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPasswd() {
        return this.passwd;
    }

    public int getServerPort() {
        return this.serverport;
    }

    public List<String> getSchemas() {
        return this.schemas;
    }

    public Map<String, List<TableConfig>> getTableConfigs() {
        return tableConfigs;
    }

    @Override
    public String toString() {
        return "XmlServerLoader{" +
                "databaseXmlPath='" + databaseXmlPath + '\'' +
                ", schemas=" + schemas +
                ", tableConfigs=" + tableConfigs +
                ", userName='" + userName + '\'' +
                ", passwd='" + passwd + '\'' +
                ", serverport=" + serverport +
                '}';
    }
}
