package alchemystar.engine.net.response;

import alchemystar.engine.net.handler.frontend.FrontendConnection;
import alchemystar.engine.net.proto.mysql.EOFPacket;
import alchemystar.engine.net.proto.mysql.FieldPacket;
import alchemystar.engine.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.engine.net.proto.mysql.RowDataPacket;
import alchemystar.engine.net.proto.util.Fields;
import alchemystar.engine.net.proto.util.LongUtil;
import alchemystar.engine.net.proto.util.PacketUtil;
import alchemystar.engine.parser.util.ParseUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * SelectIndentity
 *
 * @Author lizhuyang
 */
public class SelectIdentity {

    private static final int FIELD_COUNT = 1;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    static {
        byte packetId = 0;
        header.packetId = ++packetId;
    }

    public static void response(FrontendConnection c, String stmt, int aliasIndex, final String orgName) {
        ChannelHandlerContext ctx = c.getCtx();
        String alias = ParseUtil.parseAlias(stmt, aliasIndex);
        if (alias == null) {
            alias = orgName;
        }

        ByteBuf buffer = ctx.alloc().buffer();

        // write header
        buffer = header.writeBuf(buffer, ctx);

        // write fields
        byte packetId = header.packetId;
        FieldPacket field = PacketUtil.getField(alias, orgName, Fields.FIELD_TYPE_LONGLONG);
        field.packetId = ++packetId;
        buffer = field.writeBuf(buffer, ctx);

        // write eof
        EOFPacket eof = new EOFPacket();
        eof.packetId = ++packetId;
        buffer = eof.writeBuf(buffer, ctx);

        // write rows
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(LongUtil.toBytes(c.getLastInsertId()));
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, ctx);

        // post write
        ctx.writeAndFlush(buffer);
    }

}