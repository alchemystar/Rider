package alchemystar.parser;

import alchemystar.engine.Session;

/**
 * @Author lizhuyang
 */
public class NoOperation extends Prepared {

    public NoOperation(Session session) {
        super(session);
    }

    @Override
    public boolean isQuery() {
        return super.isQuery();
    }
}
