package alchemystar.table;

import alchemystar.access.Cursor;
import alchemystar.access.CursorFactory;
import alchemystar.engine.Session;
import alchemystar.expression.ConditionAndOr;
import alchemystar.expression.Expression;
import alchemystar.parser.dml.Select;
import alchemystar.value.Value;
import alchemystar.value.ValueNull;

/**
 * represents a table that is used in a query
 * with the condition
 *
 * @Author lizhuyang
 */
public class TableFilter implements ColumnResolver {
    // query state machine
    private static final int BEFORE_FIRST = 0, FOUND = 1, AFTER_LAST = 2, NULL_ROW = 3;

    private Table table;
    // the expression to filter rows
    private Expression filterCondition;

    private Expression joinCondition;
    /**
     * The joined table (if there is one).
     */
    private TableFilter join;

    private Row current;
    private Cursor cursor;
    private int state;
    private String alias;
    private Select select;
    private int scanCount = 0;
    private boolean foundOne;

    private Session session;

    public TableFilter(Session session, Table table, String alias, Select select) {
        this.table = table;
        this.alias = alias;
        this.select = select;
        cursor = CursorFactory.newInstance(session, table);
    }

    public void addFilterCondition(Expression condition, boolean isJoin) {
        if (isJoin) {
            if (joinCondition == null) {
                joinCondition = condition;
            } else {
                joinCondition = new ConditionAndOr(ConditionAndOr.AND, joinCondition, condition);
            }
        } else {
            if (filterCondition == null) {
                filterCondition = condition;
            } else {
                filterCondition = new ConditionAndOr(ConditionAndOr.AND, filterCondition, condition);
            }
        }
    }

    public void startQuery(Session s) {
        this.session = s;
        scanCount = 0;
    }

    public Column[] getColumns() {
        return table.getColumns();
    }

    public Value getValue(Column column) {
        int columnId = column.getColumnId();
        if (current == null) {
            current = cursor.get();
            if (current == null) {
                return ValueNull.INSTANCE;
            }
        }
        return current.getValue(columnId);
    }

    public TableFilter getTableFilter() {
        return this;
    }

    /**
     * 为以后的表别名做准备
     *
     * @return
     */
    public String getTableAlias() {
        return alias;
    }

    public Table getTable() {
        return table;
    }

    public void reset() {
        if (join != null) {
            join.reset();
        }
        state = BEFORE_FIRST;
        foundOne = false;
    }

    /**
     * Check if there are more rows to read
     *
     * @return
     */
    public boolean next() {
        if (state == AFTER_LAST) {
            return false;
        } else if (state == BEFORE_FIRST) {
            // 递归join下去,将所有都reset
            if (join != null) {
                join.reset();
            }
        } else {
            // state == FOUND || NULL_ROW
            // 如果找到一个,则定住这个table的这条记录,然后到下一个join了的tableFilter上面
            if (join != null && join.next()) {
                return true;
            }
        }
        while (true) {
            if (state == NULL_ROW) {
                break;
            }
            if (cursor.next()) {
                current = cursor.get();
                state = FOUND;
            } else {
                state = AFTER_LAST;
            }

            // NOT Support NestJoin
            if (state == AFTER_LAST) {
                // Not Support Join
                break;
            }
            if (!isOk(filterCondition)) {
                continue;
            }
            // 在这一步check 当前row是否符合joinCondition
            boolean joinConditionOk = isOk(joinCondition);
            if (state == FOUND) {
                if (joinConditionOk) {
                    foundOne = true;
                } else {
                    continue;
                }
            }
            if (join != null) {
                // found one 后 reset其后所有的join
                join.reset();
                // join(另一个table)的读取row
                if (!join.next()) {
                    // 表明join读不到了,continue
                    continue;
                }
            }
            // 当前返回true的时候就停在Table1.currentRow1,Table2.currentRow2......上
            if (state == NULL_ROW || joinConditionOk) {
                return true;
            }

        }
        state = AFTER_LAST;
        return false;
    }

    private boolean isOk(Expression condition) {
        if (condition == null) {
            return true;
        }
        return Boolean.TRUE.equals(condition.getBooleanValue(session));
    }

    /**
     * Set the Current Row
     *
     * @param current
     */
    public void set(Row current) {
        this.current = current;
    }

    public void addJoin(TableFilter filter, boolean outer, final Expression on) {
        if (on != null) {
            on.mapColumns(this, 0);
        }
        if (join == null) {
            join = filter;
            // NOT support outer join
            if (on != null) {
                mapAndAddFilter(on);
            }
        } else {
            join.addJoin(filter, outer, on);
        }
    }

    public void mapAndAddFilter(Expression on) {
        on.mapColumns(this, 0);
        addFilterCondition(on, true);
        // 递归的map下去
        if (join != null) {
            join.mapAndAddFilter(on);
        }
    }

    public TableFilter getJoin() {
        return join;
    }

    public Expression getJoinCondition() {
        return joinCondition;
    }

    public void removeJoinCondition() {
        this.joinCondition = null;
    }

    public void removeJoin() {
        this.join = null;
    }
}

