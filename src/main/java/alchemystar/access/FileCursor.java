package alchemystar.access;

import alchemystar.engine.Session;
import alchemystar.storage.fs.FileStorage;
import alchemystar.table.Row;
import alchemystar.table.Table;

/**
 * FileCursor
 * Read From file
 *
 * @Author lizhuyang
 */
public class FileCursor extends BaseCursor {

    protected String filePath;
    protected FileStorage fileStorage;

    public FileCursor(Session session, Table table) {
        super(session, table);
        this.filePath = session.getTablePath(table.getSchema().getName(), table.getName());
        fileStorage = new FileStorage(filePath, table);
    }

    public Row get() {
        return fileStorage.get();
    }

    public boolean next() {
        return fileStorage.next();
    }

    public boolean previous() {
        return fileStorage.previous();
    }

    public void init() {

    }
}
