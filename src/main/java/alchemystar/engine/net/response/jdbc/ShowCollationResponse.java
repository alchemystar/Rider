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
public class ShowCollationResponse {

    private static final int FIELD_COUNT = 6;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("Collation", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Charset", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Id", Fields.FIELD_TYPE_LONGLONG);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Default", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Compiled", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Sortlen", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void response(FrontendConnection c) {
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

        RowDataPacket row = genOneRow("gb2312_chinese_ci", "gb2312", 28, c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);

        row = genOneRow("utf8_general_ci", "utf8", 33, c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        // write lastEof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, ctx);

        // write buffer
        ctx.writeAndFlush(buffer);
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
