package alchemystar.parser.dml;

import java.util.ArrayList;

import alchemystar.engine.Session;
import alchemystar.expression.ConditionAndOr;
import alchemystar.expression.Expression;
import alchemystar.expression.ExpressionColumn;
import alchemystar.expression.WildCard;
import alchemystar.parser.Query;
import alchemystar.result.LocalResult;
import alchemystar.result.ResultTarget;
import alchemystar.table.Column;
import alchemystar.table.Table;
import alchemystar.table.TableFilter;
import alchemystar.util.CompareUtil;
import alchemystar.value.Value;
import alchemystar.value.ValueLong;

/**
 * This class represents a simple SELECT statement.
 *
 * @Author lizhuyang
 */
public class Select extends Query {

    private TableFilter topTableFilter;
    private final ArrayList<TableFilter> filters = new ArrayList<TableFilter>();
    private final ArrayList<TableFilter> topFilters = new ArrayList<TableFilter>();
    private ArrayList<Expression> expressions;
    private Expression[] expressionArray;
    private Expression condition;
    private int visibleColumnCount;
    private Boolean checkInit;
    private int currentRowNumber;

    public Select(Session session) {
        super(session);
    }

    /**
     * TableFilters For the join
     */
    public void addTableFilter(TableFilter filter, boolean isTop) {
        filters.add(filter);
        if (isTop) {
            topFilters.add(filter);
        }
    }

    public ArrayList<TableFilter> getTopFilters() {
        return topFilters;
    }

    public void setExpressions(ArrayList<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public void init() {
        expandColumnList();
        // *转译后的结果
        visibleColumnCount = expressions.size();
        // map columns in select list and condition
        for (TableFilter f : filters) {
            for (Expression expr : expressions) {
                // finally give expressionColumn columnResolver and level
                expr.mapColumns(f, 0);
            }
            if (condition != null) {
                condition.mapColumns(f, 0);
            }
        }
        expressionArray = new Expression[expressions.size()];
        expressions.toArray(expressionArray);
        checkInit = true;
    }

    private void expandColumnList() {
        for (int i = 0; i < expressions.size(); i++) {
            Expression expr = expressions.get(i);
            if (!expr.isWildcard()) {
                continue;
            }
            // 如果有*通配符号,则直接把所有通配符加上tableName扔进去
            String tableAlias = expr.getTableAlias();
            // 表明就是个简单的*,还没有被下面的代码重新WildCard过
            if (!((WildCard) expr).isWildInit()) {
                int temp = i;
                expressions.remove(i);
                for (TableFilter filter : filters) {
                    WildCard c2 = new WildCard(filter.getTableAlias(), true);
                    // 对所有的table都增加wildCard
                    expressions.add(i++, c2);
                }
                // 由于remove(i)=>取到下一个表达式-1
                i = temp - 1;
            } else {
                TableFilter filter = null;
                // 找出对应当前expr对应的TableFilter
                for (TableFilter f : filters) {
                    if (CompareUtil.equalsIdentifiers(tableAlias, f.getTableAlias())) {
                        filter = f;
                        break;
                    }
                }
                if (filter == null) {
                    throw new RuntimeException("Can't find the apponited filter,tableName=" + tableAlias);
                }
                Table t = filter.getTable();
                String alias = filter.getTableAlias();
                // 转译WildCard
                expressions.remove(i);
                // 将所有的ColumnExpresison加进去
                Column[] columns = t.getColumns();
                for (Column c : columns) {
                    ExpressionColumn ec = new ExpressionColumn(alias, c.getName());
                    expressions.add(i++, ec);
                }
                // 由于remove了WildCard,所以减1
                i--;
            }

        }
    }

    public void addCondition(Expression cond) {
        if (condition == null) {
            condition = cond;
        } else {
            condition = new ConditionAndOr(ConditionAndOr.AND, cond, condition);
        }
    }

    @Override
    protected LocalResult queryWithoutCache(ResultTarget target) {
        long limitRows = -1;
        if (limitExpr != null) {
            Value v = limitExpr.getValue(session);
            limitRows = ((ValueLong) v).getLong();

        }
        int columnCount = expressions.size();
        LocalResult result = null;
        if (target == null) {
            result = createLocalResult(result);
        }
        // no sort
        // no distinct
        // no group
        // no limit
        topTableFilter = topFilters.get(0);
        topTableFilter.startQuery(session);
        topTableFilter.reset();
        queryFlat(columnCount, result, limitRows);
        if (result != null) {
            result.done();
            if (target != null) {
                target.addRow(result.currentRow());
            }
            result.close();
            return result;
        }
        return null;

    }

    private void queryFlat(int columnCount, ResultTarget result, long limitRows) {
        long offset = -1;
        if (offsetExpr != null) {
            Value v = offsetExpr.getValue(session);
            offset = ((ValueLong) v).getLong() * limitRows;

        }

        int rowNumber = 0;
        currentRowNumber = 0;
        while (topTableFilter.next()) {
            currentRowNumber = rowNumber + 1;
            // Filter the row with Condition
            if (condition == null || Boolean.TRUE.equals(condition.getBooleanValue(session))) {
                Value[] row = new Value[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    Expression expression = expressions.get(i);
                    row[i] = expression.getValue(session);
                }
                rowNumber++;
                if (offset > 0 && rowNumber <= offset) {
                    // skip the offset
                    continue;
                } else {
                    // limitRows=-1 表示没有limit
                    if ((limitRows > 0 && result.getRowCount() < limitRows) || limitRows == -1) {
                        result.addRow(row);
                    } else {
                        break;
                    }
                }
            }
            // Not Sort
        }
    }

    public int getColumnCount() {
        return visibleColumnCount;
    }

    public ArrayList<Expression> getExpressions() {
        return expressions;
    }

    private LocalResult createLocalResult(LocalResult old) {
        return old != null ? old : new LocalResult(session, expressionArray, visibleColumnCount);
    }
}
