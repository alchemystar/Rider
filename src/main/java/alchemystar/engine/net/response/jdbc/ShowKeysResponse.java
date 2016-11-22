package alchemystar.engine.net.response.jdbc;

import alchemystar.engine.Database;
import alchemystar.engine.net.handler.frontend.FrontendConnection;
import alchemystar.engine.net.proto.mysql.EOFPacket;
import alchemystar.engine.net.proto.mysql.FieldPacket;
import alchemystar.engine.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.engine.net.proto.mysql.RowDataPacket;
import alchemystar.engine.net.proto.util.Fields;
import alchemystar.engine.net.proto.util.PacketUtil;
import alchemystar.engine.net.proto.util.StringUtil;
import alchemystar.schema.Schema;
import alchemystar.table.Column;
import alchemystar.table.Table;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author lizhuyang
 */
public class ShowKeysResponse {

    private static final int FIELD_COUNT = 13;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("Table", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Non_unique", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Key_name", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Seq_in_index", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Column_name", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Collation", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Cardinality", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Sub_part", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Packed", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Null", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Index_type", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Comment", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Index_comment", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void response(FrontendConnection c, String stmt) {
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
        Byte packetId = eof.packetId;

        Table table = getTable(stmt);
        // 以第一个作为primaryKey
        Column primaryColumn = table.getColumns()[0];
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(table.getName().getBytes());
        row.add("0".getBytes());
        row.add("PRIMARY".getBytes());
        row.add("1".getBytes());
        row.add(primaryColumn.getName().getBytes());
        row.add("A".getBytes());
        row.add("0".getBytes());
        row.add("Sub_part".getBytes());
        row.add("Packed".getBytes());
        row.add("".getBytes());
        row.add("Index_type".getBytes());
        row.add("Comment".getBytes());
        row.add("Index_comment".getBytes());
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);

        // write lastEof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, ctx);

        // write buffer
        ctx.writeAndFlush(buffer);
    }

    private static Table getTable(String stmt) {
        Database database = Database.getInstance();
        String stmtTrim = stmt.trim();
        String[] array = stmtTrim.split("FROM");
        String tableName = formatString(array[1]);
        String schemaName = getSchema(array[2]);
        Schema schema = database.findSchema(schemaName);
        if (schema == null) {
            throw new RuntimeException("Schema not Found,SchemaName=" + schemaName);
        }
        Table table = schema.getTableOrView(tableName);
        if (table == null) {
            throw new RuntimeException("Table not Found,TableName=" + tableName);
        }
        return table;
    }

    private static String formatString(String s) {
        String strim = s.trim();
        String result = strim;
        if (strim.startsWith("'")) {
            result = strim.substring(1, strim.length() - 1);
        }
        if (strim.startsWith("`")) {
            result = strim.substring(1, strim.length() - 1);
        }

        return result;
    }

    private static String getSchema(String schemaString) {
        String[] s1 = schemaString.split("LIKE");
        return formatString(formatString(s1[0]));
    }

    private static RowDataPacket genOneRow(String value1, String value2, Integer index, FrontendConnection c) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(value1, c.getCharset()));
        row.add(StringUtil.encode(value2, c.getCharset()));
        row.add(index.toString().getBytes());
        row.add("Yes".getBytes());
        row.add("Yes".getBytes());
        row.add("1".getBytes());
        return row;
    }
}
