package alchemystar.parser;

import java.util.ArrayList;

import alchemystar.engine.Session;
import alchemystar.expression.Expression;
import alchemystar.result.LocalResult;
import alchemystar.result.ResultTarget;

/**
 * Now Only Support Query
 *
 * @Author lizhuyang
 */
public abstract class Query extends Prepared {

    // for limit
    protected Expression offsetExpr;
    // for limit
    protected Expression limitExpr;

    private LocalResult lastResult;

    protected Query(Session session) {
        super(session);
    }

    public LocalResult query() {
        return query(null);
    }

    /**
     * Initialize the query.
     */
    public abstract void init();

    /**
     * Execute the query, writing the result to the target result.
     *
     * @param target
     *
     * @return
     */
    LocalResult query(ResultTarget target) {
        return queryWithoutCache(target);
    }

    protected abstract LocalResult queryWithoutCache(ResultTarget target);

    public void setLimit(Expression limit) {
        this.limitExpr = limit;
    }

    public void setOffset(Expression offset) {
        this.offsetExpr = offset;
    }

    public Expression getLimitExpr() {
        return limitExpr;
    }

    public void setLimitExpr(Expression limitExpr) {
        this.limitExpr = limitExpr;
    }

    public abstract int getColumnCount();

    public abstract ArrayList<Expression> getExpressions();
}
