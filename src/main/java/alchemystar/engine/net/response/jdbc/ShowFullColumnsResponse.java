package alchemystar.engine.net.response.jdbc;

import alchemystar.engine.Database;
import alchemystar.engine.net.handler.frontend.FrontendConnection;
import alchemystar.engine.net.proto.mysql.EOFPacket;
import alchemystar.engine.net.proto.mysql.FieldPacket;
import alchemystar.engine.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.engine.net.proto.mysql.RowDataPacket;
import alchemystar.engine.net.proto.util.Fields;
import alchemystar.engine.net.proto.util.PacketUtil;
import alchemystar.schema.Schema;
import alchemystar.table.Column;
import alchemystar.table.Table;
import alchemystar.value.Value;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author lizhuyang
 */
public class ShowFullColumnsResponse {

    private static final int FIELD_COUNT = 9;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("Field", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("Type", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("Collation", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("Null", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("Key", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("Default", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("Extra", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("Privileges", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("Comment", Fields.FIELD_TYPE_VAR_STRING);
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
        byte packetId = eof.packetId;

        Table table = getTable(stmt);

        for (int i = 0; i < table.getColumns().length; i++) {
            RowDataPacket row = genColumnPacket(table.getColumns()[i], i, c.getCharset());
            row.packetId = ++packetId;
            buffer = row.writeBuf(buffer, ctx);
        }

        // write lastEof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, ctx);

        // write buffer
        ctx.writeAndFlush(buffer);
    }

    private static RowDataPacket genColumnPacket(Column column, int index, String charset) {
        RowDataPacket rowDataPacket = new RowDataPacket(FIELD_COUNT);
        rowDataPacket.add(column.getName().getBytes());
        rowDataPacket.add(getType(column.getType()));
        if (column.getType() == Value.STRING) {
            if (charset.toUpperCase().equals("UTF-8") || charset.toUpperCase().equals("UTF8")) {
                rowDataPacket.add("utf8_general_ci".getBytes());
            } else {
                rowDataPacket.add("gb2312".getBytes());
            }
        } else {
            rowDataPacket.add("NULL".getBytes());
        }
        rowDataPacket.add(getNull());
        if (index == 0) {
            // 让第一个column做主键
            rowDataPacket.add(getKey(true));
        } else {
            rowDataPacket.add(getKey(false));
        }
        rowDataPacket.add(getDefault(column.getType()));
        rowDataPacket.add(getExtra());
        rowDataPacket.add(getPrivileges());
        rowDataPacket.add(column.getComment().getBytes());
        return rowDataPacket;
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

    public static void main(String args[]) {
        String s1 = " SHOW FULL COLUMNS FROM `t_db_info` FROM `mystique_test` LIKE '%'";
        String[] array1 = s1.split("FROM");
        for (int i = 0; i < array1.length; i++) {
            System.out.println(i + ":" + array1[i]);
        }
    }

    private static byte[] getKey(Boolean isPrimary) {
        if (isPrimary) {
            return "PRI".getBytes();
        } else {
            return "".getBytes();
        }
    }

    private static byte[] getNull() {
        return "NO".getBytes();
    }

    private static byte[] getType(int type) {
        switch (type) {
            case Value.INT:
                return "int(11)".getBytes();
            case Value.LONG:
                return "bigint(20)".getBytes();
            case Value.STRING:
                return "Varchar(64)".getBytes();
            default:
                // Only Support (INT|LONG|STRING),default varchar
                return "Varchar(64)".getBytes();
        }
    }

    private static byte[] getPrivileges() {
        return "select,insert,update,references".getBytes();
    }

    private static byte[] getDefault(int type) {
        if (type == Value.INT || type == Value.LONG) {
            return "0".getBytes();
        } else {
            return "".getBytes();
        }
    }

    private static byte[] getExtra() {
        return "".getBytes();
    }

}
