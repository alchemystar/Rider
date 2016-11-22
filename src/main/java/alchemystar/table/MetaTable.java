package alchemystar.table;

import java.util.ArrayList;

import alchemystar.access.MetaCursor;
import alchemystar.engine.Database;
import alchemystar.engine.Session;
import alchemystar.schema.Schema;
import alchemystar.value.DataType;
import alchemystar.value.Value;
import alchemystar.value.ValueNull;
import alchemystar.value.ValueString;

/**
 * MetaTable
 *
 * @Author lizhuyang
 */
public class MetaTable extends Table {

    private static final int TABLES = 0;
    private static final int COLUMNS = 1;
    // private static final int INDEXES = 2;
    // private static final int TABLE_TYPES = 3;
    // private static final int TYPE_INFO = 4;
    private static final int META_TABLE_TYPE_COUNT = COLUMNS + 1;

    private Database database;
    private MetaCursor metaCursor;

    private final int type;

    public MetaTable(Schema schema, int id, int type) {
        super(schema, id, null, null, false);
        Column[] cols = null;
        this.type = type;
        switch (type) {
            case TABLES:
                super.setName("TABLES");
                cols = createColumns(
                        "TABLE_CATALOG",
                        "TABLE_SCHEMA",
                        "TABLE_NAME",
                        "TABLE_TYPE",
                        "ENGINE",
                        // extensions
                        "STORAGE_TYPE",
                        "SQL",
                        "REMARKS",
                        "LAST_MODIFICATION BIGINT",
                        "ID INT",
                        "TYPE_NAME",
                        "TABLE_CLASS"
                );
                break;
            case COLUMNS:
                super.setName("COLUMNS");
                cols = createColumns(
                        "TABLE_CATALOG",
                        "TABLE_SCHEMA",
                        "TABLE_NAME",
                        "COLUMN_NAME",
                        "ORDINAL_POSITION INT",
                        "COLUMN_DEFAULT",
                        "IS_NULLABLE",
                        "DATA_TYPE INT",
                        "CHARACTER_MAXIMUM_LENGTH INT",
                        "CHARACTER_OCTET_LENGTH INT",
                        "NUMERIC_PRECISION INT",
                        "NUMERIC_PRECISION_RADIX INT",
                        "NUMERIC_SCALE INT",
                        "CHARACTER_SET_NAME",
                        "COLLATION_NAME",
                        // extensions
                        "TYPE_NAME",
                        "NULLABLE INT",
                        "IS_COMPUTED BYTE",
                        "SELECTIVITY INT",
                        "CHECK_CONSTRAINT",
                        "SEQUENCE_NAME",
                        "REMARKS",
                        "SOURCE_DATA_TYPE INT"
                );
                break;
           /* case INDEXES:
                super.setName("INDEXES");
                cols = createColumns(
                        "TABLE_CATALOG",
                        "TABLE_SCHEMA",
                        "TABLE_NAME",
                        "NON_UNIQUE BIT",
                        "INDEX_NAME",
                        "ORDINAL_POSITION SMALLINT",
                        "COLUMN_NAME",
                        "CARDINALITY INT",
                        "PRIMARY_KEY BIT",
                        "INDEX_TYPE_NAME",
                        "IS_GENERATED BIT",
                        "INDEX_TYPE SMALLINT",
                        "ASC_OR_DESC",
                        "PAGES INT",
                        "FILTER_CONDITION",
                        "REMARKS",
                        "SQL",
                        "ID INT",
                        "SORT_TYPE INT",
                        "CONSTRAINT_NAME",
                        "INDEX_CLASS"
                );
                break;
            case TABLE_TYPES:
                super.setName("TABLE_TYPES");
                cols = createColumns("TYPE");
                break;
            case TYPE_INFO:
                super.setName("TYPE_INFO");
                cols = createColumns(
                        "TYPE_NAME",
                        "DATA_TYPE INT",
                        "PRECISION INT",
                        "PREFIX",
                        "SUFFIX",
                        "PARAMS",
                        "AUTO_INCREMENT BIT",
                        "MINIMUM_SCALE SMALLINT",
                        "MAXIMUM_SCALE SMALLINT",
                        "RADIX INT",
                        "POS INT",
                        "CASE_SENSITIVE BIT",
                        "NULLABLE SMALLINT",
                        "SEARCHABLE SMALLINT"
                );
                break;*/
            default:
                throw new RuntimeException("Unknown table type=" + type);
        }
        setColumns(cols);
        setTableEngine("information");
        metaCursor = new MetaCursor(this);
    }

    private Column[] createColumns(String... names) {
        Column[] cols = new Column[names.length];
        for (int i = 0; i < names.length; i++) {
            String nameType = names[i];
            String name = null;
            int idx = nameType.indexOf(' ');
            int dataType;
            if (idx < 0) {
                dataType = Value.STRING;
                name = nameType;
            } else {
                dataType = DataType.getTypeByName(nameType.substring(idx + 1));
                name = nameType.substring(0, idx);
            }
            cols[i] = new Column(dataType, name);
        }
        return cols;
    }

    public ArrayList<Row> generateRows(Session session) {
        ArrayList<Row> rows = new ArrayList<Row>();
        switch (type) {
            case TABLES:
                for (Table table : getAllTables()) {
                    // todo meta table
                }
                break;
            default:
                throw new RuntimeException("No Suceh Meta Table");

        }
        return null;
    }

    public static int getMetaTableTypeCount() {
        return META_TABLE_TYPE_COUNT;
    }

    private ArrayList<Table> getAllTables() {
        return database.getAllTablesAndViews(true);
    }

    private void add(ArrayList<Row> rows, String... strings) {
        Value[] values = new Value[strings.length];
        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];
            Value v = (s == null) ? (Value) ValueNull.INSTANCE : ValueString.get(s);
            Column col = columns[i];
            v = col.convert(v);
            values[i] = v;
        }
        Row row = new Row(values);
        rows.add(row);
    }

}
