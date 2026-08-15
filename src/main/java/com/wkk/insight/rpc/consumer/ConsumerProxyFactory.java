package com.wkk.insight.rpc.consumer;

import com.wkk.insight.rpc.core.RequestEncoder;
import com.wkk.insight.rpc.core.WKKDecoder;
import com.wkk.insight.rpc.exception.RpcException;
import com.wkk.insight.rpc.protocol.Request;
import com.wkk.insight.rpc.protocol.Response;
import com.wkk.insight.rpc.register.DefaultServiceRegister;
import com.wkk.insight.rpc.register.ServiceMetadata;
import com.wkk.insight.rpc.register.ServiceRegister;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Slf4j
public class ConsumerProxyFactory {

    // 在途请求
    private final Map<Integer, CompletableFuture<Response>> inFlightRequests;

    private final ConnectionManager manager;

    private final ServiceRegister register;

    private ConsumerProperties consumerProperties;


    public ConsumerProxyFactory(ConsumerProperties consumerProperties) throws Exception {
        this.register = new DefaultServiceRegister();
        this.register.init(consumerProperties.getRegistryConfig());
        this.manager= new ConnectionManager(createBootstrap(consumerProperties));
        this.inFlightRequests = new ConcurrentHashMap<>();
        this.consumerProperties = consumerProperties;

    }

    public <I> I createConsumerProxy(Class<I> interfaceClass) {
        return (I) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{interfaceClass},
                new ConsumerInvocationHandler(interfaceClass));

    }

    public class ConsumerInvocationHandler implements InvocationHandler {

        final Class<?> interfaceClass;

        public ConsumerInvocationHandler(Class<?> interfaceClass) {
            this.interfaceClass = interfaceClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return invokeObjectMethod(proxy, method, args);
            }
            CompletableFuture<Response> responseFuture = new CompletableFuture<>();
            try {
                List<ServiceMetadata> serviceMetadata = register.fetchServiceList(interfaceClass.getName());
                if (serviceMetadata.isEmpty()) {
                    throw new RpcException(interfaceClass.getName() + "没有对应的provider");
                }
                ServiceMetadata providerMetadata = serviceMetadata.get(0);
                Channel channel = manager.getChannel(providerMetadata.getHost(), providerMetadata.getPort());
                if (channel == null) {
                    throw new RpcException("provider 连接失败");
                }
                Request request = buildRequest(method, args);
                inFlightRequests.put(request.getRequestId(), responseFuture);
                channel.writeAndFlush(request).addListener(f -> {
                    if (!f.isSuccess()) {
                        inFlightRequests.remove(request.getRequestId());
                        responseFuture.completeExceptionally(f.cause());
                    }
                });
                Response response = responseFuture.get(consumerProperties.getRequestTimeoutMs(), TimeUnit.MILLISECONDS);
                return processResponse(response);
            } catch (RpcException rpcException) {
                throw rpcException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Object processResponse(Response response) {
            if (response.getCode() == 200) {
                return response.getResult();
            }
            throw new RpcException(response.getErrorMessage());
        }

        private Request buildRequest(Method method, Object[] args) {
            Request request = new Request();
            request.setMethodName(method.getName());
            request.setParams(args);
            request.setParameterTypes(method.getParameterTypes());
            request.setServiceName(interfaceClass.getName());
            return request;
        }

        private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("toString")) {
                return "Proxy Consumer " + interfaceClass.getName();
            }
            if (method.getName().equals("equals")) {
                return proxy == args[0];
            }
            if (method.getName().equals("hashCode")) {
                return System.identityHashCode(proxy);
            }
            throw new UnsupportedOperationException("代理对象不支持这个函数" + method.getName());
        }
    }

    private Bootstrap createBootstrap(ConsumerProperties consumerProperties) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(consumerProperties.getWorkThreadNum()))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, consumerProperties.getConnectTimeoutMs())
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline()
                                .addLast(new WKKDecoder())
                                .addLast(new RequestEncoder())
                                .addLast(new ConsumerHandler());
                    }
                });
        return bootstrap;
    }

    private class ConsumerHandler extends SimpleChannelInboundHandler<Response>{
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Response response) throws Exception {
            CompletableFuture<Response> responseFuture = inFlightRequests.remove(response.getRequestId());
            if (responseFuture == null) {
                log.warn("request Id{}找不到", response.getRequestId());
                return;
            }
            responseFuture.complete(response);
        }
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("地址:{}连接了", ctx.channel().remoteAddress());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("地址:{} 断开了连接", ctx.channel().remoteAddress());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("发生了异常", cause);
            ctx.channel().close();
        }
    }
}
