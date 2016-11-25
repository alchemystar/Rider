package alchemystar.schema;

import java.util.ArrayList;
import java.util.HashMap;

import alchemystar.engine.Database;
import alchemystar.parser.ddl.CreateTableData;
import alchemystar.table.RegularTable;
import alchemystar.table.Table;

/**
 * Schema
 *
 * @Author lizhuyang
 */
public class Schema {
    private Database database;
    private String name;
    private boolean system;
    private final HashMap<String, Table> tablesAndViews;

    public Schema(boolean system, Database database, String name) {
        this.system = system;
        this.database = database;
        tablesAndViews = new HashMap<String, Table>();
        this.name = name;
    }

    public Table getTableOrView(String name) {
        Table table = tablesAndViews.get(name);
        return table;
    }

    public void addTable(Table table) {
        synchronized(this) {
            if (tablesAndViews.get(table.getName()) != null) {
                throw new RuntimeException("Duplicated Table:" + table.getName());
            }
            tablesAndViews.put(table.getName(), table);
        }
    }

    public void coverTable(Table table) {
        synchronized(this) {
            tablesAndViews.put(table.getName(), table);
        }
    }

    // For meta
    public ArrayList<Table> getAllTablesAndViews() {
        return new ArrayList<Table>(tablesAndViews.values());
    }

    public Table createTable(CreateTableData data) {
        // 做一次同步
        synchronized(this) {
            data.schema = this;
            // different engine
           /* if(data.tableEngine != null){
            }*/
            return new RegularTable(data);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Table> getTablesAndViews() {
        return tablesAndViews;
    }
}
