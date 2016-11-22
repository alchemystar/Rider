package alchemystar.result;

import alchemystar.value.Value;

/**
 * @Author lizhuyang
 */
public interface ResultInterface {
    /**
     * Go to the beginning of the result
     */
    void reset();

    /**
     * Get the current row
     *
     * @return
     */
    Value[] currentRow();

    /**
     * Go to the next row
     *
     * @return
     */
    boolean next();

    /**
     * Get the number of rows in this object
     *
     * @return
     */
    int getRowCount();

    /**
     * Get the table name for the column
     *
     * @param i
     *
     * @return
     */
    String getTableName(int i);

    /**
     * Get the column name
     *
     * @param i
     *
     * @return
     */
    String getColumnName(int i);

    /**
     * Get the column data type
     *
     * @param i
     *
     * @return
     */
    int getColumnType(int i);

    /**
     * Get the current row id, starting with 0.
     * -1 is returned when next() was not called yet.
     *
     * @return
     */
    public int getRowId();
}
