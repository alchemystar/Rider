package alchemystar.table;

import alchemystar.value.Value;

/**
 * @Author lizhuyang
 */
public interface ColumnResolver {

    Column[] getColumns();

    Value getValue(Column column);

    TableFilter getTableFilter();
}
