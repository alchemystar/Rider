package alchemystar.engine.net.response;

import java.util.ArrayList;

import alchemystar.engine.Database;
import alchemystar.engine.Session;
import alchemystar.engine.net.handler.frontend.FrontendConnection;
import alchemystar.engine.net.proto.mysql.EOFPacket;
import alchemystar.engine.net.proto.mysql.FieldPacket;
import alchemystar.engine.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.engine.net.proto.mysql.RowDataPacket;
import alchemystar.engine.net.proto.util.Fields;
import alchemystar.engine.net.proto.util.PacketUtil;
import alchemystar.engine.net.proto.util.StringUtil;
import alchemystar.schema.Schema;
import alchemystar.table.Table;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author lizhuyang
 */
public class ShowCreateTable {
    private static final int FIELD_COUNT = 2;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("Table", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Create Table", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void response(FrontendConnection c, String stmt) {
        Session session = c.getSession();
        ChannelHandlerContext ctx = c.getCtx();
        ByteBuf buffer = ctx.alloc().buffer();

        // write header
        buffer = header.writeBuf(buffer, ctx);

        // write fields
        for (FieldPacket field : fields) {
            buffer = field.writeBuf(buffer, ctx);
        }

        // write eof
        buffer = eof.writeBuf(buffer, ctx);

        // write rows
        byte packetId = eof.packetId;

        String tableName = getTableName(stmt);
        Table table = getTable(session, tableName);
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(tableName, c.getCharset()));
        row.add(StringUtil.encode(table.getOriginSql(), c.getCharset()));
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);

        // write lastEof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, ctx);

        // write buffer
        ctx.writeAndFlush(buffer);
    }

    private static String getTableName(String stmt) {
        return stmt.trim().substring(17).trim();
    }

    private static Table getTable(Session session, String tableName) {
        String schemaName = session.getCurrentSchemaName();
        if (schemaName == null) {
            throw new RuntimeException(" No database selected");
        }
        Database database = Database.getInstance();
        ArrayList<String> list = new ArrayList<String>();
        Schema schema = database.findSchema(schemaName);
        if (schema == null) {
            throw new RuntimeException(" No Such Database");
        }
        Table table = schema.getTableOrView(tableName);
        if (table == null) {
            throw new RuntimeException("No Such Table:" + tableName + " in schema:" + schemaName);
        }
        return table;
    }
}
