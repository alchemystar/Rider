package alchemystar.engine.net.handler.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.engine.net.proto.util.ErrorCode;
import alchemystar.engine.net.response.SelectTxResponse;
import alchemystar.engine.parser.ServerParse;

/**
 * ServerQueryHandler
 *
 * @Author lizhuyang
 */
public class ServerQueryHandler implements FrontendQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerQueryHandler.class);

    private FrontendConnection source;

    public ServerQueryHandler(FrontendConnection source) {
        this.source = source;
    }

    public void query(String origin) {

        logger.debug("sql = " + origin);
        String sql = removeFirstAnnotation(origin);
        int rs = ServerParse.parse(sql);
        switch (rs & 0xff) {
            case ServerParse.SET:
                SetHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParse.SHOW:
                ShowHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParse.SELECT:
                SelectHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParse.START:
                StartHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParse.BEGIN:
                BeginHandler.handle(sql, source);
                break;
            case ServerParse.SAVEPOINT:
                SavepointHandler.handle(sql, source);
                break;
            case ServerParse.KILL:
                KillHandler.handle(sql, rs >>> 8, source);
                break;
            case ServerParse.KILL_QUERY:
                source.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
                break;
            case ServerParse.USE:
                UseHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParse.EXPLAIN:
                source.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
                break;
            case ServerParse.CREATE_DATABASE:
                source.createShema(sql);
                break;
            case ServerParse.COMMIT:
                source.commit();
                break;
            case ServerParse.ROLLBACK:
                source.rollBack();
                break;
            default:
                // todo add no modify exception
                source.execute(sql, rs);
        }
    }

    public static void main(String args[]) {
        String sql = "/* mysql-connector-java-5.1.26 ( Revision: ${bzr.revision-id} ) */SHOW VARIABLES WHERE "
                + "Variable_name ='language' OR Variable_name = 'net_write_timeout' OR Variable_name = "
                + "'interactive_timeout' OR Variable_name = 'wait_timeout' OR Variable_name = 'character_set_client' "
                + "OR Variable_name = 'character_set_connection' OR Variable_name = 'character_set' OR Variable_name "
                + "= 'character_set_server' OR Variable_name = 'tx_isolation' OR Variable_name = "
                + "'transaction_isolation' OR Variable_name = 'character_set_results' OR Variable_name = 'timezone' "
                + "OR Variable_name = 'time_zone' OR Variable_name = 'system_time_zone' OR Variable_name = "
                + "'lower_case_table_names' OR Variable_name = 'max_allowed_packet' OR Variable_name = "
                + "'net_buffer_length' OR Variable_name = 'sql_mode' OR Variable_name = 'query_cache_type' OR "
                + "Variable_name = 'query_cache_size' OR Variable_name = 'init_connect'";
        String sql2 = "SHOW VARIABLES WHERE Variable_name ='language' OR Variable_name = 'net_write_timeout' OR "
                + "Variable_name = 'interactive_timeout' OR Variable_name = 'wait_timeout' OR Variable_name = "
                + "'character_set_client' OR Variable_name = 'character_set_connection' OR Variable_name = "
                + "'character_set' OR Variable_name = 'character_set_server' OR Variable_name = 'tx_isolation' OR "
                + "Variable_name = 'transaction_isolation' OR Variable_name = 'character_set_results' OR "
                + "Variable_name = 'timezone' OR Variable_name = 'time_zone' OR Variable_name = 'system_time_zone' OR"
                + " Variable_name = 'lower_case_table_names' OR Variable_name = 'max_allowed_packet' OR Variable_name"
                + " = 'net_buffer_length' OR Variable_name = 'sql_mode' OR Variable_name = 'query_cache_type' OR "
                + "Variable_name = 'query_cache_size' OR Variable_name = 'license' OR Variable_name = 'init_connect'";
        System.out.println(ServerQueryHandler.removeFirstAnnotation(sql));
        System.out.println(sql2);

    }

    public static String removeFirstAnnotation(String sql) {
        String result = null;
        sql = sql.trim();
        if (sql.startsWith("/*")) {
            int index = sql.indexOf("*/") + 2;
            return sql.substring(index);
        } else {
            return sql;
        }
    }
}
