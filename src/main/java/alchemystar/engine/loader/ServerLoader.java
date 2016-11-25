package alchemystar.engine.loader;

import java.util.List;
import java.util.Map;

/**
 * @Author lizhuyang
 */
public interface ServerLoader {

    String getUserName();

    String getPasswd();

    int getServerPort();

    List<String> getSchemas();

    Map<String, List<TableConfig>> getTableConfigs();
}
