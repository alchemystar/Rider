package alchemystar;

import alchemystar.engine.Database;
import alchemystar.engine.Session;
import alchemystar.mock.MockTable;
import alchemystar.parser.Parser;
import alchemystar.parser.Prepared;
import alchemystar.schema.Schema;

/**
 * @Author lizhuyang
 */
public class CreateTableTest {

    public static void main(String[] args) {
        String sql = "create table if not exists t_archer(\n"
                + "\tid BIGINT comment 'id test',\n"
                + "\tname VARCHAR comment 'name test'\n"
                + ")Engine=default comment='just for test'";
        Session session = getSession();
        Parser parser = new Parser(session);
        Prepared prepared = parser.parse(sql);
        prepared.update();
        Database database = session.getDatabase();
        System.out.println(prepared);

    }

    public static Session getSession() {
        Database database = Database.getInstance();
        Schema test = new Schema(false, database, "test");
        test.addTable(MockTable.getTable(test));
        database.addSchema(test);
        Session session = new Session(database, "SHUAIQI", 1);
        session.setCurrentSchema(test);
        return session;
    }
}
