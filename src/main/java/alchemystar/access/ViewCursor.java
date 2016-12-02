package alchemystar.access;

import alchemystar.engine.Session;
import alchemystar.storage.view.ViewStorage;
import alchemystar.table.Row;
import alchemystar.table.Table;

/**
 * 视图Cursor
 *
 * @Author lizhuyang
 */
public class ViewCursor extends BaseCursor {

    private ViewStorage viewStorage;

    public ViewCursor(Session session, Table table) {
        super(session, table);
        init();
    }

    public Row get() {
        return viewStorage.get();
    }

    public boolean next() {
        return viewStorage.next();
    }

    public boolean previous() {
        return viewStorage.previous();
    }

    public void init() {
        viewStorage = new ViewStorage(sesson, table);
    }
}
