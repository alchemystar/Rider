package alchemystar.engine.net.response.jdbc;

import alchemystar.engine.net.handler.frontend.FrontendConnection;
import alchemystar.engine.net.proto.mysql.EOFPacket;
import alchemystar.engine.net.proto.mysql.FieldPacket;
import alchemystar.engine.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.engine.net.proto.mysql.RowDataPacket;
import alchemystar.engine.net.proto.util.Fields;
import alchemystar.engine.net.proto.util.PacketUtil;
import alchemystar.engine.net.proto.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author lizhuyang
 */
public class ShowFullTablesResponse {

    private static final int FIELD_COUNT = 2;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("Tables", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("table_type", Fields.FIELD_TYPE_VAR_STRING);
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

        RowDataPacket row = genOneRow(getTableName(stmt), "BASE TABLE", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);

        // write lastEof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, ctx);

        // write buffer
        ctx.writeAndFlush(buffer);
    }

    public static String getTableName(String stmt) {
        int pos = stmt.indexOf("LIKE");
        // 4 for like , and 1 for 空格
        String result = stmt.substring(pos + 4 + 1);
        if (result.startsWith("'")) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    private static RowDataPacket genOneRow(String value1, String value2, FrontendConnection c) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(value1, c.getCharset()));
        row.add(StringUtil.encode(value2, c.getCharset()));
        return row;
    }
}
