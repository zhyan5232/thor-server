package com.thor.node.network.codec;

import com.thor.node.network.protocol.ThorMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ThorDecoder extends ByteToMessageDecoder {
    private static final Logger log = LoggerFactory.getLogger(ThorDecoder.class);
    private static final int MAGIC_NUMBER = 0x54484f52; // THOR

    private enum State { READ_HEADER, READ_PAYLOAD, READ_STREAM }
    private State currentState = State.READ_HEADER;
    private int currentType;
    private int currentLength;
    private int streamReadProgress = 0;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (true) {
            switch (currentState) {
                case READ_HEADER:
                    if (in.readableBytes() < 12) return;
                    in.markReaderIndex();
                    int magic = in.readInt();
                    if (magic != MAGIC_NUMBER) {
                        log.error("非法魔数: 0x{}", Integer.toHexString(magic));
                        ctx.close();
                        return;
                    }
                    currentType = in.readInt();
                    currentLength = in.readInt();
                    currentState = (currentType == ThorMessage.TYPE_FILE_STREAM) ? State.READ_STREAM : State.READ_PAYLOAD;
                    break;

                case READ_PAYLOAD:
                    if (in.readableBytes() < currentLength) return;
                    byte[] payload = new byte[currentLength];
                    in.readBytes(payload);
                    out.add(new ThorMessage(currentType, payload));
                    currentState = State.READ_HEADER;
                    break;

                case READ_STREAM:
                    int available = Math.min(in.readableBytes(), currentLength - streamReadProgress);
                    if (available > 0) {
                        byte[] chunk = new byte[available];
                        in.readBytes(chunk);
                        out.add(new ThorMessage(currentType, chunk));
                        streamReadProgress += available;
                    }
                    if (streamReadProgress >= currentLength) currentState = State.READ_HEADER;
                    else return;
                    break;
            }
            if (!in.isReadable()) break;
        }
    }
}