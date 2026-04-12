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
import lombok.extern.slf4j.Slf4j;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Slf4j
public class ProviderServer {

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    private final int port;

    private ProviderRegistry providerRegistry;

    public ProviderServer(int port) {
        this.port = port;
        this.providerRegistry = new ProviderRegistry();
    }

    public <I> void register(Class<I> interfaceClass, I serviceInstance) {
        providerRegistry.register(interfaceClass, serviceInstance);
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
                                    .addLast(new ProviderHandler());
                        }
                    });

            serverBootstrap.bind(this.port).sync();
        } catch (Exception e) {
            throw new RuntimeException("服务异常" + e);
        }

    }

    public class ProviderHandler extends SimpleChannelInboundHandler<Request> {

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Request request) throws Exception {
            // 并不是所有的方法都能被执行，譬如provider的私有方法等，所以需要一个注册表维护可用方法，使用接口进行约定支持使用的方法
            System.out.println("get request" + " " + request);
            ProviderRegistry.Invocation<?> service = providerRegistry.findService(request.getServiceName());
            if (service == null) {
                Response fail = Response.fail("not found service " + request.getServiceName(), request.getRequestId());
                channelHandlerContext.writeAndFlush(fail);
                return;
            }
            try {
                Object result = service.invoke(request.getMethodName(), request.getParameterTypes(), request.getParams());
                Response success = Response.success(result, request.getRequestId());
                log.info("{} 函数被调用, result: {}", request.getServiceName(), result);
                channelHandlerContext.writeAndFlush(success);
            } catch (Exception e) {
                Response fail = Response.fail(e.getMessage(), request.getRequestId());
                channelHandlerContext.writeAndFlush(fail);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("地址：{} 链接了", ctx.channel().remoteAddress());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("地址：{} 断开了链接", ctx.channel().remoteAddress());

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.info("exception", cause);
            ctx.channel().close();

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
