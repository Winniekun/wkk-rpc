package com.wkk.insight.rpc.core;

import com.alibaba.fastjson2.JSONObject;
import com.wkk.insight.rpc.protocol.Message;
import com.wkk.insight.rpc.protocol.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class ResponseEncoder extends MessageToByteEncoder<Response> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Response msg, ByteBuf out) throws Exception {
        // length
        // logic
        // type
        // response
        byte[] response = serializeResponse(msg);

        int messageLength = response.length + Message.MESSAGE_LOGIC.length + Byte.BYTES;

        out.writeInt(messageLength);
        out.writeBytes(Message.MESSAGE_LOGIC);
        out.writeByte(Message.MessageType.RESPONSE.getNumber());
        out.writeBytes(response);
    }

    private byte[] serializeResponse(Response msg) {
        return JSONObject.toJSONString(msg).getBytes(StandardCharsets.UTF_8);
    }
}
