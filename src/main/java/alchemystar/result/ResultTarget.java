package alchemystar.result;

import alchemystar.value.Value;

/**
 * A object where rows are written to
 *
 * @Author lizhuyang
 */
public interface ResultTarget {
    /**
     * Add the row to the result set
     *
     * @param value
     */
    void addRow(Value[] value);

    /**
     * Get the number of rows
     *
     * @return
     */
    int getRowCount();
}
