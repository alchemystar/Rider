package alchemystar.access;

import alchemystar.engine.Session;
import alchemystar.table.Table;

/**
 * @Author lizhuyang
 */
public abstract class BaseCursor implements Cursor {

    protected Session sesson;
    protected Table table;

    public BaseCursor(Session session, Table table) {
        this.sesson = session;
        this.table = table;
    }

}
