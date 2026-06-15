package com.thor.node.network.codec;

import com.thor.node.network.protocol.ThorMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ThorEncoder extends MessageToByteEncoder<ThorMessage> {
    private static final int MAGIC_NUMBER = 0x54484f52;

    @Override
    protected void encode(ChannelHandlerContext ctx, ThorMessage msg, ByteBuf out) {
        out.writeInt(MAGIC_NUMBER);
        out.writeInt(msg.getType());
        out.writeInt(msg.getLength());
        if (msg.getPayload() != null && msg.getPayload().length > 0) {
            out.writeBytes(msg.getPayload());
        }
    }
}