package alchemystar.table;

import alchemystar.value.Value;

/**
 * Row 行的代表
 *
 * @Author lizhuyang
 */
public class Row {
    // data contains the actually data
    private Value[] data;

    public Row(Value[] data) {
        this.data = data;
    }

    public Value getValue(int i) {
        return data[i];
    }

    public void setValue(int i, Value v) {
        data[i] = v;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < data.length; i++) {
            if (data[i].getType() == Value.LONG) {
                s += data[i].getLong() + " ";
            }
            if (data[i].getType() == Value.STRING) {
                s += data[i].getString() + " ";
            }
        }
        return s;
    }
}
