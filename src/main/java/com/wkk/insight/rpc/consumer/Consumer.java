package com.wkk.insight.rpc.consumer;

import com.wkk.insight.rpc.core.RequestEncoder;
import com.wkk.insight.rpc.core.ResponseEncoder;
import com.wkk.insight.rpc.core.WKKDecoder;
import com.wkk.insight.rpc.protocol.Request;
import com.wkk.insight.rpc.protocol.Response;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
public class Consumer {

    public int add(int a, int b) throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> addResult = new CompletableFuture<>();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(4))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline()
                                .addLast(new WKKDecoder())
                                .addLast(new RequestEncoder())
                                .addLast(new SimpleChannelInboundHandler<Response>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Response message) throws Exception {
//                                        int res = Integer.parseInt(message);
                                        System.out.println("consumer " +  message);
                                        int result = Integer.parseInt((String) message.getResult());
                                        addResult.complete(result);
                                    }
                                });
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect("localhost", 8888).sync();
        Request request = new Request();
        request.setServiceName("serviceName");
        request.setMethodName("method");
        request.setParams(new Object[]{a, b});
        request.setParameterTypes(new String[]{"int", "int"});
        channelFuture.channel().writeAndFlush(request);
        return addResult.get();
    }
}
