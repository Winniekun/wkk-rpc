package com.wkk.insight.rpc.provider;

import com.wkk.insight.rpc.core.ResponseEncoder;
import com.wkk.insight.rpc.core.WKKDecoder;
import com.wkk.insight.rpc.protocol.Request;
import com.wkk.insight.rpc.protocol.Response;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class ProviderServer {

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    private final int port;

    public ProviderServer(int port) {
        this.port = port;
    }

    public void start() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(4);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        //   message  --->  黏包半包 --->  解码成string  ---> 根据string信息调用对应的内容 --> 调用真正的函数 -->  拿到返回值返回
                        protected void initChannel(SocketChannel nioServerSocketChannel) throws Exception {
                            nioServerSocketChannel.pipeline()
                                    .addLast(new WKKDecoder())
                                    .addLast(new ResponseEncoder())
                                    .addLast(new SimpleChannelInboundHandler<Request>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Request message) throws Exception {
                                            System.out.println("get message" +  " " + message);
                                            Response response = new Response();
                                            response.setResult("1");
                                            channelHandlerContext.writeAndFlush(response);
                                        }
                                    });
                        }
                    });

            serverBootstrap.bind(this.port).sync();
        } catch (Exception e) {
            throw new RuntimeException("服务异常" + e);
        }

    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    private static int add(int a, int b) {
        return a + b;
    }
}
