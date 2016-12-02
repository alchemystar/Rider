package alchemystar.access;

import alchemystar.engine.Session;
import alchemystar.engine.net.proto.util.StringUtil;
import alchemystar.mock.MockTable;
import alchemystar.table.Table;

/**
 * CursorFactory
 *
 * @Author lizhuyang
 */
public class CursorFactory {

    public static Cursor newInstance(Session session, Table table) {
        if (table.getTableEngine() == null) {
            // Default Simple
            return new FileCursor(session, table);
        }
        String tableEngine = table.getTableEngine().toUpperCase();
        if ("DEFAULT".equals(tableEngine)) {
            return new FileCursor(session, table);
        }
        if ("MOCK".equals(tableEngine)) {
            return new SimpleCursor(MockTable.getRows());
        }
        if ("INFORMATION".equals(tableEngine)) {
            return new MetaCursor(table);
        }
        if (StringUtil.isEmpty(table.getViewSql())) {
            return new FileCursor(session, table);
        } else {
            // 视图用
            return new ViewCursor(session, table);
        }
    }
}
