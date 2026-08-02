package com.wkk.insight.rpc.consumer;

import com.wkk.insight.rpc.api.Add;
import com.wkk.insight.rpc.core.RequestEncoder;
import com.wkk.insight.rpc.core.WKKDecoder;
import com.wkk.insight.rpc.exception.RpcException;
import com.wkk.insight.rpc.protocol.Request;
import com.wkk.insight.rpc.protocol.Response;
import com.wkk.insight.rpc.register.DefaultServiceRegister;
import com.wkk.insight.rpc.register.RegisterConfig;
import com.wkk.insight.rpc.register.ServiceMetadata;
import com.wkk.insight.rpc.register.ServiceRegister;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
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

/**
 * 类描述: TODO
 *
 * @author weikunkun
 */
@Slf4j
public class ConsumerProxyFactory {

    // 在途请求
    private final Map<Integer, CompletableFuture<Response>> inFlightRequests = new ConcurrentHashMap<>();

    private final ConnectionManager connectionManager = new ConnectionManager(createBootstrap());

    private final ServiceRegister register;


    public ConsumerProxyFactory(RegisterConfig registerConfig) throws Exception {
        this.register = new DefaultServiceRegister();
        this.register.init(registerConfig);
    }


    private Bootstrap createBootstrap() {
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
                                        CompletableFuture<Response> requestFuture = inFlightRequests.remove(response.getRequestId());
                                        if (requestFuture == null) {
                                            log.error("request 找不到: {}", response.getRequestId());
                                            return;
                                        }
                                        requestFuture.complete(response);
                                    }
                                });
                    }
                });
        return bootstrap;
    }

    public <I> I getConsumerProxy(Class<I> interfaceClass) {
        return (I) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
                    throw new UnsupportedOperationException("不支持该方法调用" + method.getName());
                }
                try {
                    CompletableFuture<Response> addResult = new CompletableFuture<>();
                    List<ServiceMetadata> serviceMetadata = register.fetchServiceList(interfaceClass.getName());
                    if (serviceMetadata.isEmpty()) {
                        throw new RpcException(interfaceClass.getName() + "没有对应的provider");
                    }
                    ServiceMetadata providerMetadata = serviceMetadata.get(0);
                    // 改为从注册中心获取可用的服务地址
                    Channel channel = connectionManager.getChannel(providerMetadata.getHost(), providerMetadata.getPort());
                    if (channel == null) {
                        throw new RpcException("provider 链接失败");
                    }
                    Request request = new Request();
                    request.setServiceName(interfaceClass.getName());
                    request.setMethodName(method.getName());
                    request.setParams(args);
                    request.setParameterTypes(method.getParameterTypes());
                    channel.writeAndFlush(request).addListener((f) -> {
                        if (f.isSuccess()) {
                            inFlightRequests.put(request.getRequestId(), addResult);
                        }
                    });
                    Response response = addResult.get();
                    if (response.getCode() == 200) {
                        return response.getResult();
                    }
                    throw new RpcException(response.getErrorMessage());
                } catch (RpcException rpcException) {
                    throw new RpcException(rpcException.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
