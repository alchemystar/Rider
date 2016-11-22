package alchemystar.parser;

import alchemystar.engine.Session;
import alchemystar.result.ResultInterface;
import alchemystar.util.BitField;

/**
 * @Author lizhuyang
 */
public abstract class Prepared {

    protected Session session;
    private int objectId;
    protected String sqlStatement;
    protected boolean create = true;

    public Prepared(Session session) {
        this.session = session;
    }

    public boolean isQuery() {
        return false;
    }

    public ResultInterface query() {
        throw new RuntimeException("abstract class , not support this function");
    }

    public int update() {
        throw new RuntimeException("Method not allowed for query");
    }

    protected int getObjectId() {
        int id = objectId;
        if (id == 0) {
            id = session.getDatabase().allocateObjectId();
        } else {
            objectId = 0;
        }
        return id;
    }
}
