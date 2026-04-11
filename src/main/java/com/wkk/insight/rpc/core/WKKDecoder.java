package com.wkk.insight.rpc.core;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
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
        byte[] magic = new byte[Message.MESSAGE_LOGIC.length];
        frame.readBytes(magic);
        if (!Arrays.equals(magic, Message.MESSAGE_LOGIC)) {
            throw new RuntimeException("协议错误！,可能链接了错误的服务器");
        }
        byte messageType = frame.readByte();
        byte[] body = new byte[frame.readableBytes()];
        frame.readBytes(body);
        if (Objects.equals(Message.MessageType.REQUEST.getNumber(), messageType)) {
            return deserializeRequest(body);
        }
        if (Objects.equals(Message.MessageType.RESPONSE.getNumber(), messageType)) {
            return deserializeResponse(body);

        }
        throw new RuntimeException("协议错误！,可能链接了错误的服务器");
    }


    private Response deserializeResponse(byte[] body) {
        return JSONObject.parseObject(new String(body, StandardCharsets.UTF_8), Response.class);
    }

    private Request deserializeRequest(byte[] body) {
        return JSONObject.parseObject(new String(body, StandardCharsets.UTF_8), Request.class,
                JSONReader.Feature.SupportClassForName);
    }

}
