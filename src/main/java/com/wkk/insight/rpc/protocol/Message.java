package com.wkk.insight.rpc.protocol;

import lombok.Data;

import java.nio.charset.StandardCharsets;

/**
 * 类描述: 消息体
 *
 * @author weikunkun
 */
@Data
public class Message {

    public static final byte[] MESSAGE_LOGIC = "无牙仔".getBytes(StandardCharsets.UTF_8);

    /**
     * 魔数校验
     */
    private byte[] logic;

    /**
     * 消息类型
     */
    private byte messageType;

    /**
     * 消息体
     */
    private byte[] body;


    public enum MessageType {
        REQUEST(1),

        RESPONSE(2);

        private final byte number;

        MessageType(int number) {
            this.number = (byte) number;
        }

        public byte getNumber() {
            return number;
        }
    }
}
