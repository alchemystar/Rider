package alchemystar.parser.ddl;

import java.util.ArrayList;

import alchemystar.engine.Session;
import alchemystar.engine.config.SystemConfig;
import alchemystar.schema.Schema;
import alchemystar.table.Column;

/**
 * @Author lizhuyang
 */
public class CreateTableData {

    /**
     * The schema
     */
    public Schema schema;

    /**
     * The table name;
     */
    public String tableName;

    /**
     * The object id
     */
    public int id;

    public ArrayList<Column> columns = new ArrayList<Column>();

    /**
     * Wether to create a new table
     */
    public boolean create;

    /**
     * The session
     */
    public Session sesison;

    /**
     * The table engine to use for creating the table
     */
    public String tableEngine;

    /**
     * The table is hidden
     */
    public boolean isHidden;

    /**
     * 默认seperator是逗号,默认csv
     */
    public String seperator = ",";

    /**
     * 默认skip掉错误的行
     */
    public boolean skipWrong = false;

    public String charset = SystemConfig.DEFAULT_CHARSET;

    /**
     * Xlsx 文件
     */
    public int sheetNumber = 0;
}
