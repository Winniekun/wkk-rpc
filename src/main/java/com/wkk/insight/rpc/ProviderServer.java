package com.wkk.insight.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class ProviderServer {



    public void start() {

    }

    public void stop() {

    }

    public static void main(String[] args) throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup(4))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel nioServerSocketChannel) throws Exception {
                        nioServerSocketChannel.pipeline()
                                .addLast(new LineBasedFrameDecoder(1024))
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new SimpleChannelInboundHandler<String>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String message) throws Exception {
                                        String[] split = message.split(",");
                                        String method = split[0];
                                        int a = Integer.parseInt(split[1]);
                                        int b = Integer.parseInt(split[2]);
                                        if ("add".equals(method)) {
                                            int add = add(a, b);
                                            channelHandlerContext.writeAndFlush(add + "\n");
                                        }
                                    }
                                });
                    }
                });

        serverBootstrap.bind(8888).sync();
    }

    private static int add(int a, int b) {
        return a + b;
    }
}
