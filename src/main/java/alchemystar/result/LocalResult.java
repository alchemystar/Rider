package alchemystar.result;

import java.util.ArrayList;

import alchemystar.engine.Session;
import alchemystar.expression.Expression;
import alchemystar.value.Value;

/**
 * A local result set contans all row data of a result set
 * todo
 * If the result does not fit in memory, it is written to a temporary file.
 *
 * @Author lizhuyang
 */
public class LocalResult implements ResultInterface, ResultTarget {

    private Session session;
    private Expression[] expressions;
    int rowId;
    private int rowCount;
    private ArrayList<Value[]> rows;
    private Value[] currentRow;
    private int offset;
    private int limit = -1;
    private boolean closed;
    private int visibleColumnCount;

    public LocalResult() {
        // nothing to do
    }

    public LocalResult(Session session, Expression[] expressions, int visibleColumnCount) {
        this.session = session;
        this.expressions = expressions;
        rows = new ArrayList<Value[]>();
        this.visibleColumnCount = visibleColumnCount;
        rowId = -1;
    }

    public void reset() {
        rowId = -1;
    }

    public Value[] currentRow() {
        return currentRow;
    }

    public boolean next() {
        if (rowId < rowCount) {
            rowId++;
            if (rowId < rowCount) {
                currentRow = rows.get(rowId);
                return true;
            }
            // to the end, so set to null
            currentRow = null;
        }
        return false;
    }

    public int getRowId() {
        return rowId;
    }

    public int getRowCount() {
        return rowCount;
    }

    public String getTableName(int i) {
        return expressions[i].getTableName();
    }

    public String getColumnName(int i) {
        return expressions[i].getColumnName();
    }

    public int getColumnType(int i) {
        return expressions[i].getType();
    }

    public void addRow(Value[] values) {
        rows.add(values);
        rowCount++;
        // todo external in disk
    }

    public void done() {
        applyOffset();
        applyLimit();
        reset();
    }

    private void applyOffset() {
        if (offset <= 0) {
            return;
        }

        if (offset >= rows.size()) {
            rows.clear();
            rowCount = 0;
        } else {
            // avoid copying the whole array for each row
            int remove = Math.min(offset, rows.size());
            rows = new ArrayList<Value[]>(rows.subList(remove, rows.size()));
            rowCount -= remove;
        }

    }

    private void applyLimit() {
        if (limit < 0) {
            return;
        }
        if (rows.size() > limit) {
            rows = new ArrayList<Value[]>(rows.subList(0, limit));
            rowCount = limit;
        }
    }

    public void close() {
        closed = true;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public ArrayList<Value[]> getRows() {
        return rows;
    }

    public void setRows(ArrayList<Value[]> rows) {
        this.rows = rows;
    }
}
