package alchemystar.expression;

import alchemystar.engine.Session;
import alchemystar.table.ColumnResolver;
import alchemystar.util.RiderStringUtils;
import alchemystar.value.Value;

/**
 * @Author lizhuyang
 */
public class WildCard extends Expression {

    private final String table;
    private boolean wildInit;

    public WildCard(String table) {
        this.table = table;
    }

    public WildCard(String table, Boolean wildInit) {
        this.table = table;
        this.wildInit = wildInit;
    }

    public boolean isWildcard() {
        return true;
    }

    @Override
    public int getType() {
        throw new RuntimeException("WildCard can't support getType");
    }

    @Override
    public Value getValue(Session session) {
        throw new RuntimeException("WildCard can't support getValue");

    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        throw new RuntimeException("WildCard can't support mapColumns");
    }

    public String getTableAlias() {
        return table;
    }

    public String getSQL() {
        if (table == null) {
            return "*";
        }
        return RiderStringUtils.quoteIdentifier(table) + ".*";
    }

    public boolean isWildInit() {
        return wildInit;
    }

    public void setWildInit(boolean wildInit) {
        this.wildInit = wildInit;
    }
}
