package alchemystar.mock;

import alchemystar.schema.Schema;
import alchemystar.table.Column;
import alchemystar.table.Row;
import alchemystar.table.Table;
import alchemystar.value.Value;
import alchemystar.value.ValueLong;
import alchemystar.value.ValueString;

/**
 * @Author lizhuyang
 */
public class MockTable {

    public static final String MOCK_TABLE = "t_mock_table";

    public static Table getTable(Schema schema) {
        Table mockTable = new Table(schema, 0, MOCK_TABLE, ",", false);
        mockTable.setTableEngine("MOCK");
        Column[] columns = new Column[2];
        Column columnId = new Column(Value.LONG, "id");
        Column columnName = new Column(Value.STRING, "name");
        columns[0] = columnId;
        columns[1] = columnName;
        mockTable.setColumns(columns);
        return mockTable;
    }

    public static Row[] getRows() {
        Row[] rows = new Row[3];
        Value valueColumn1 = ValueLong.get(1L);
        Value valueName1 = ValueString.get("archer");
        Value[] valueList0 = new Value[] {valueColumn1, valueName1};
        rows[0] = new Row(valueList0);
        Value valueColumn2 = ValueLong.get(2L);
        Value valueName2 = ValueString.get("saber");
        Value[] valueList2 = new Value[] {valueColumn2, valueName2};
        rows[1] = new Row(valueList2);
        Value valueColumn3 = ValueLong.get(3L);
        Value valueName3 = ValueString.get("lancer");
        Value[] valueList3 = new Value[] {valueColumn3, valueName3};
        rows[2] = new Row(valueList3);
        return rows;
    }

    public static void main(String args[]) {

    }
}
