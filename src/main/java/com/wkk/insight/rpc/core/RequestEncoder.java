package com.wkk.insight.rpc.core;

import com.alibaba.fastjson2.JSONObject;
import com.wkk.insight.rpc.protocol.Message;
import com.wkk.insight.rpc.protocol.Request;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class RequestEncoder extends MessageToByteEncoder<Request> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Request message, ByteBuf out) throws Exception {
        // length
        // logic
        // type
        // request
        byte[] request = serializeRequest(message);

        int messageLength = request.length + Message.MESSAGE_LOGIC.length + Byte.BYTES;

        out.writeInt(messageLength);
        out.writeBytes(Message.MESSAGE_LOGIC);
        out.writeByte(Message.MessageType.REQUEST.getNumber());
        out.writeBytes(request);
    }

    private byte[] serializeRequest(Request msg) {
        return JSONObject.toJSONString(msg).getBytes(StandardCharsets.UTF_8);
    }
}
