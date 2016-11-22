package alchemystar.table;

import alchemystar.parser.ddl.CreateTableData;

/**
 * Most tables are this instance
 *
 * @Author lizhuyang
 */
public class RegularTable extends TableBase {

    public RegularTable(CreateTableData data) {
        super(data);
    }
}
