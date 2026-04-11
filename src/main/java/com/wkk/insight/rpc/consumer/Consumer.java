package com.wkk.insight.rpc.consumer;

import com.wkk.insight.rpc.api.Add;
import com.wkk.insight.rpc.core.RequestEncoder;
import com.wkk.insight.rpc.core.ResponseEncoder;
import com.wkk.insight.rpc.core.WKKDecoder;
import com.wkk.insight.rpc.exception.RpcException;
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
public class Consumer implements Add {

    @Override
    public int add(int a, int b)  {
        try {
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
                                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Response response) throws Exception {
                                            System.out.println("response: " + response);
                                            if (response.getCode() == 200) {
                                                Integer result = Integer.valueOf(response.getResult().toString());
                                                addResult.complete(result);
                                            } else {
                                                String errorMessage = response.getErrorMessage();
                                                addResult.completeExceptionally(new RpcException(errorMessage));
                                            }
                                        }
                                    });
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8888).sync();
            Request request = new Request();
            request.setServiceName(Add.class.getName());
            request.setMethodName("add");
            request.setParameterTypes(new Class[]{int.class, int.class});
            request.setParams(new Object[]{a, b});
            channelFuture.channel().writeAndFlush(request);
            return addResult.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
