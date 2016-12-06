package alchemystar.access;

import java.util.ArrayList;

import alchemystar.table.Row;
import alchemystar.table.Table;

/**
 * @Author lizhuyang
 */
public class MetaCursor implements Cursor {

    private Row current;
    private ArrayList<Row> rows;
    private int index;
    Table meta;

    public MetaCursor(Table meta) {
        this.meta = meta;
        init();
    }

    public Row get() {
        return current;
    }

    public Row getSearchRow() {
        return current;
    }

    public boolean next() {
        current = index >= rows.size() ? null : rows.get(index++);
        return current != null;
    }

    public void init() {

    }

    @Override
    public void reset() {

    }

    public boolean previous() {
        throw new RuntimeException("MetaCursor not support previous");
    }
}
