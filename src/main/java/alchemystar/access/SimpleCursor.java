package alchemystar.access;

import alchemystar.table.Row;

/**
 * 最简单的游标
 *
 * @Author lizhuyang
 */
public class SimpleCursor implements Cursor {

    private Row[] rows;
    /**
     * 当前scan到的row
     */
    private int current;

    public SimpleCursor(Row[] rows) {
        this.rows = rows;
        current = -1;
    }

    public Row get() {
        return rows[current];
    }

    public boolean next() {
        if (current < rows.length - 1) {
            current += 1;
            return true;
        }
        return false;
    }

    public boolean previous() {
        if (current > 0) {
            current = current - 1;
            return true;
        }
        return false;
    }

    public void init() {
        // do nothing
    }
}
