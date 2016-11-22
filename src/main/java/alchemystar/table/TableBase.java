package alchemystar.table;

import alchemystar.parser.ddl.CreateTableData;
import alchemystar.util.RiderStringUtils;
import alchemystar.util.StatementBuilder;

/**
 * The base class
 * Main the getCreateSql
 *
 * @Author lizhuyang
 */
public class TableBase extends Table {

    public TableBase(CreateTableData data) {
        super(data.schema, data.id, data.tableName, data.seperator, data.skipWrong);
        super.tableEngine = data.tableEngine;
        Column[] cols = new Column[data.columns.size()];
        data.columns.toArray(cols);
        setColumns(cols);
    }

    public String getDropSQL() {
        return "DROP TABLE IF EXISTS " + getSQL() + " CASCADE";
    }

    public String getCreateSQL() {
        StatementBuilder buff = new StatementBuilder("CREATE ");
        buff.append("TABLE ");
        if (isHidden) {
            buff.append("IF NOT EXISTS ");
        }
        buff.append(getSQL());
        buff.append("(\n   ");
        for (Column column : columns) {
            buff.appendExceptFirst(",\n   ");
            buff.append(column.getCreateSQL());
        }
        buff.append("\n)");
        if (tableEngine != null) {
            buff.append("ENGINE=\"");
            buff.append(tableEngine);
            buff.append('\"');
        }
        if (isHidden) {
            buff.append("\nHIDDEN");
        }
        if (comment != null) {
            buff.append(" COMMENT ").append("=").append(RiderStringUtils.quoteStringSQL(comment));
        }
        return buff.toString();
    }

    public String getSQL() {
        return name;
    }
}
