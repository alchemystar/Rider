package alchemystar.storage.view;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.engine.Session;
import alchemystar.parser.Parser;
import alchemystar.parser.dml.Select;
import alchemystar.result.LocalResult;
import alchemystar.table.Row;
import alchemystar.table.Table;
import alchemystar.value.Value;

/**
 * 从别的执行SQL那获取Row
 *
 * @Author lizhuyang
 */
public class ViewStorage {
    private static final Logger logger = LoggerFactory.getLogger(ViewStorage.class);

    // 当前FileStorage都在Session里
    private ArrayList<Value[]> rows;
    private int current;
    private Session session;
    private Table table;
    private String viewSql;

    public ViewStorage(Session session, Table table) {
        // 最多defaultRowsInMemory在缓存中
        rows = new ArrayList<Value[]>();
        current = -1;
        this.session = session;
        this.table = table;
        this.viewSql = table.getViewSql();
        initRead();
    }

    public void initRead() {
        Parser parser = new Parser(session);
        Select query = (Select) parser.parse(viewSql);
        LocalResult result = query.query();
        rows = result.getRows();

    }

    public Row get() {
        return new Row(rows.get(current));
    }

    public boolean next() {
        if (current < rows.size() - 1) {
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

    public void reset() {
        current = -1;
    }

    public static void main(String args[]) {
        String s1 = "1|2|3";
        String seperator = "\\" + "|";
        String[] array = s1.split(seperator);
        for (int i = 0; i < array.length; i++) {
            System.out.println(array[i]);
        }
    }
}
