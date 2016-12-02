package alchemystar;

import alchemystar.engine.Database;
import alchemystar.engine.Session;
import alchemystar.parser.Parser;
import alchemystar.parser.Prepared;
import alchemystar.result.ResultInterface;
import alchemystar.schema.Schema;
import alchemystar.value.Value;

/**
 * @Author lizhuyang
 */
public class ParserTest {

    public static void main(String args[]) {
        Database database = Database.getInstance();
        Schema test = new Schema(false, database, "test");
        database.addSchema(test);
        Session session = new Session(database, "SHUAIQI", 1);
        session.setCurrentSchema(test);

        String createSql = "create table if not exists t_archer(\n"
                + "\tid BIGINT comment 'id test',\n"
                + "\tname VARCHAR comment 'name test'\n"
                + ")Engine=archer comment='just for test'";
        System.out.println(createSql);
        Parser createParser = new Parser(session);
        Prepared createPrepared = createParser.parse(createSql);
        createPrepared.update();
        session.loadTable("t_archer", "/Users/alchemystar/tmp/rider/archer.txt");

        // String sql = "select * from test.t_archer";
        String sql = "select a.id,a.name,b.id,b.name,c.id,c.name from test.t_archer as a join test.t_archer "
                + "as b on a.id=b.id limit 0,100 ";
        Parser parser = new Parser(session);
        Prepared prepared = parser.parse(sql);
        ResultInterface resultInterface = prepared.query();
        while (resultInterface.next()) {
            Value[] values = resultInterface.currentRow();
            for (Value v1 : values) {
                System.out.print(v1.getString() + ",");
            }
            System.out.println();

        }
        System.out.println("parse okay");
    }

}
