package alchemystar.expression;

import alchemystar.engine.Session;
import alchemystar.table.ColumnResolver;
import alchemystar.table.TableFilter;
import alchemystar.value.Value;

/**
 * @Author lizhuyang
 */
public abstract class Expression {

    private boolean addedToFilter;

    public abstract int getType();

    // 获取当前expression的value
    public abstract Value getValue(Session session);

    public void addFilterConditions(TableFilter filter) {
        filter.addFilterCondition(this, false);
        addedToFilter = true;
    }

    public String getTableName() {
        return null;
    }

    public String getColumnName() {
        return null;
    }

    /**
     * Only returns true if the expression is a wildcard.
     *
     * @return if this expression is a wildcard
     */
    public boolean isWildcard() {
        return false;
    }

    public abstract void mapColumns(ColumnResolver resolver, int level);

    public String getTableAlias() {
        return null;
    }

    public Boolean getBooleanValue(Session session) {
        return getValue(session).getBoolean();
    }

    public abstract String getSQL();
}