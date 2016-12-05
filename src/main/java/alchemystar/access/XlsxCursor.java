package alchemystar.access;

import alchemystar.engine.Session;
import alchemystar.storage.fs.XlsxStorage;
import alchemystar.table.Row;
import alchemystar.table.Table;

/**
 * @Author lizhuyang
 */
public class XlsxCursor extends BaseCursor {

    protected String filePath;
    protected XlsxStorage xlsxStorage;

    public XlsxCursor(Session session, Table table) {
        super(session, table);
        this.filePath = session.getTablePath(table.getSchema().getName(), table.getName());
        xlsxStorage = new XlsxStorage(filePath, table);
    }

    public Row get() {
        return xlsxStorage.get();
    }

    public boolean next() {
        return xlsxStorage.next();
    }

    public boolean previous() {
        return xlsxStorage.previous();
    }

    public void init() {

    }
}
