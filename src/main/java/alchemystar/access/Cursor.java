package alchemystar.access;

import alchemystar.table.Row;

/**
 * 游标
 *
 * @Author lizhuyang
 */
public interface Cursor {
    /**
     * Get the current Row
     *
     * @return
     */
    Row get();

    /**
     * move to the next row
     *
     * @return
     */
    boolean next();

    /**
     * move to the previous row
     *
     * @return
     */
    boolean previous();

    /**
     * init
     */
    void init();

}
