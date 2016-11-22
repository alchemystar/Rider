package alchemystar.engine;

import java.util.ArrayList;
import java.util.HashMap;

import alchemystar.schema.Schema;
import alchemystar.table.MetaTable;
import alchemystar.table.Table;
import alchemystar.util.BitField;

/**
 * @Author lizhuyang
 */
public class Database {

    private final HashMap<String, Schema> schemas = new HashMap<String, Schema>();

    private final BitField objectIds = new BitField();
    // For the mybatis settings
    private Schema infoSchema;

    private static Database database = null;

    private volatile boolean metaTablesInitialized;

    // 单例模式
    static {
        database = new Database();
        Schema test = new Schema(false, database, "test");
        Schema infoSchema = new Schema(true, database, "information_schema");
        database.addSchema(test);
        database.addSchema(infoSchema);
        database.setInfoSchema(infoSchema);
        database.initMetaTables();
    }

    public ArrayList<Table> getAllTablesAndViews(boolean includeMeta) {
        if (includeMeta) {
            initMetaTables();
        }
        ArrayList<Table> list = new ArrayList<Table>();
        for (Schema schema : schemas.values()) {
            list.addAll(schema.getAllTablesAndViews());
        }
        return list;
    }

    private void initMetaTables() {
        if (metaTablesInitialized) {
            return;
        }
        synchronized(infoSchema) {
            if (!metaTablesInitialized) {
                for (int type = 0, count = MetaTable.getMetaTableTypeCount(); type < count; type++) {
                    MetaTable m = new MetaTable(infoSchema, -1 - type, type);
                    infoSchema.addTable(m);
                }
                metaTablesInitialized = true;
            }
        }
    }

    public static Database getInstance() {
        return database;
    }

    public static Session getSession() {
        Session session = new Session(database, "alchemystar", database.allocateObjectId());
        // todo CurrentSchema
        // 用test做CurrentSchema
        session.setCurrentSchema(database.findSchema("test"));
        return session;

    }

    public Schema findSchema(String schemaName) {
        Schema schema = schemas.get(schemaName);
        return schema;
    }

    public void addSchema(Schema schema) {
        synchronized(this) {
            if (schemas.get(schema.getName()) != null) {
                throw new RuntimeException("Duplicated Schema:" + schema.getName());
            }
            schemas.put(schema.getName(), schema);
        }
    }

    public void addSchema(String schemaName) {
        synchronized(this) {
            if (schemas.get(schemaName) != null) {
                throw new RuntimeException("Duplicated Schema:" + schemaName);
            }
            Schema schema = new Schema(false, this, schemaName);
            schemas.put(schemaName, schema);
        }
    }

    public void addTable(Table table) {
        table.getSchema().addTable(table);
    }

    public void coverTable(Table table) {
        table.getSchema().coverTable(table);
    }

    public synchronized int allocateObjectId() {
        int i = objectIds.nextClearBit(0);
        objectIds.set(i);
        return i;
    }

    public Schema getInfoSchema() {
        return infoSchema;
    }

    public void setInfoSchema(Schema infoSchema) {
        this.infoSchema = infoSchema;
    }

    public HashMap<String, Schema> getSchemas() {
        return schemas;
    }
}
