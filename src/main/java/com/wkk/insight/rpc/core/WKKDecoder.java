package com.wkk.insight.rpc.core;

import com.alibaba.fastjson2.JSONObject;
import com.wkk.insight.rpc.protocol.Message;
import com.wkk.insight.rpc.protocol.Request;
import com.wkk.insight.rpc.protocol.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class WKKDecoder extends LengthFieldBasedFrameDecoder {


    public WKKDecoder() {
        super(1024 * 1024, 0, 4, 0, 4);
    }

    public WKKDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        byte[] logic = new byte[Message.MESSAGE_LOGIC.length];
        frame.readBytes(logic);
        if (!Arrays.equals(logic, Message.MESSAGE_LOGIC)) {
            throw new RuntimeException("协议错误！,可能链接了错误的服务器");
        }
        byte messageType = frame.readByte();
        if (Objects.equals(Message.MessageType.REQUEST.getNumber(), messageType)) {
            return decodeRequest(frame);
        }
        if (Objects.equals(Message.MessageType.RESPONSE.getNumber(), messageType)) {
            return decodeResponse(frame);

        }
        throw new RuntimeException("协议错误！,可能链接了错误的服务器");
    }

    private Request decodeRequest(ByteBuf in) {
        String json = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();
        return JSONObject.parseObject(json, Request.class);
    }

    private Response decodeResponse(ByteBuf in) {
        String json = in.readCharSequence(in.readableBytes(),StandardCharsets.UTF_8).toString();
        return JSONObject.parseObject(json, Response.class);
    }

}
