package com.wkk.insight.rpc.consumer;

import com.google.common.collect.Lists;
import com.wkk.insight.rpc.core.RequestEncoder;
import com.wkk.insight.rpc.core.WKKDecoder;
import com.wkk.insight.rpc.exception.RpcException;
import com.wkk.insight.rpc.loadbalance.LoadBalancer;
import com.wkk.insight.rpc.loadbalance.RandomLoadBalancer;
import com.wkk.insight.rpc.loadbalance.RoundRobinLoadBalancer;
import com.wkk.insight.rpc.protocol.Request;
import com.wkk.insight.rpc.protocol.Response;
import com.wkk.insight.rpc.register.DefaultServiceRegister;
import com.wkk.insight.rpc.register.ServiceMetadata;
import com.wkk.insight.rpc.register.ServiceRegister;
import com.wkk.insight.rpc.retry.RetryContext;
import com.wkk.insight.rpc.retry.RetryPolicy;
import com.wkk.insight.rpc.retry.RetryPolicyManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    private final ConsumerProperties consumerProperties;

    private final HashedWheelTimer hashedWheelTimer;

    private final RetryPolicyManager retryPolicyManager;

    public ConsumerProxyFactory(ConsumerProperties consumerProperties) throws Exception {
        this.register = new DefaultServiceRegister();
        this.register.init(consumerProperties.getRegistryConfig());
        this.manager = new ConnectionManager(createBootstrap(consumerProperties));
        this.inFlightRequests = new ConcurrentHashMap<>();
        this.consumerProperties = consumerProperties;
        this.hashedWheelTimer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS, 256);
        this.retryPolicyManager = new RetryPolicyManager();
    }

    public <I> I createConsumerProxy(Class<I> interfaceClass) {
        return (I) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{interfaceClass},
                new ConsumerInvocationHandler(interfaceClass, createLoadBalancer(), createRetryPolicy()));

    }

    private LoadBalancer createLoadBalancer() {
        switch (this.consumerProperties.getLoadBalancePolicy()) {
            case "robin":
                return new RoundRobinLoadBalancer();
            case "random":
                return new RandomLoadBalancer();
            default:
                throw new IllegalArgumentException(this.consumerProperties.getLoadBalancePolicy() + "负载均衡不支持");
        }

    }

    private RetryPolicy createRetryPolicy() {
        RetryPolicy retryPolicy = retryPolicyManager.getRetryPolicy(this.consumerProperties.getRetryPolicy());
        if(retryPolicy == null){
            throw new IllegalArgumentException("没有这个重试策略 " + this.consumerProperties.getRetryPolicy());
        }
        return retryPolicy;

    }

    public class ConsumerInvocationHandler implements InvocationHandler {

        final Class<?> interfaceClass;

        final LoadBalancer loadBalancer;

        final RetryPolicy retryPolicy;

        public ConsumerInvocationHandler(Class<?> interfaceClass, LoadBalancer loadBalancer, RetryPolicy retryPolicy) {
            this.interfaceClass = interfaceClass;
            this.loadBalancer = loadBalancer;
            this.retryPolicy = retryPolicy;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return invokeObjectMethod(proxy, method, args);
            }
            try {
                List<ServiceMetadata> serviceMetadata = register.fetchServiceList(interfaceClass.getName());
                if (serviceMetadata.isEmpty()) {
                    throw new RpcException(interfaceClass.getName() + "没有对应的provider");
                }
                // 负载过高 每次都获取固定服务, 使用负载均衡
                ServiceMetadata providerMetadata = loadBalancer.select(serviceMetadata);
                // 添加重试机制
                Response response;
                try {
                    response = tryRpcSync(buildRequest(method, args), providerMetadata, consumerProperties.getRequestTimeoutMs());
                } catch (Exception e) {
                    // 重试
                    RetryContext retryContext = RetryContext.builder()
                            .failService(providerMetadata)
                            .serviceMetadataList(serviceMetadata)
                            .loadBalancer(loadBalancer)
                            .requestTimeoutMs(consumerProperties.getRequestTimeoutMs())
                            .methodTimeoutMs(consumerProperties.getMethodTimeoutMs())
                            .doRpcFunction(meta -> tryRpcAsync(buildRequest(method, args), meta))
                            .build();
                    response = retryPolicy.doRetry(retryContext);
                }
//                Channel channel = manager.getChannel(providerMetadata.getHost(), providerMetadata.getPort());
//                if (channel == null) {
//                    throw new RpcException("provider 连接失败");
//                }
//                inFlightRequests.put(request.getRequestId(), responseFuture);
//                channel.writeAndFlush(request).addListener(f -> {
//                    if (!f.isSuccess()) {
//                        inFlightRequests.remove(request.getRequestId());
//                        responseFuture.completeExceptionally(f.cause());
//                    }
//                });
//                Response response = responseFuture.get(consumerProperties.getRequestTimeoutMs(), TimeUnit.MILLISECONDS);
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

    private CompletableFuture<Response> tryRpcAsync(Request request, ServiceMetadata serviceMetadata) {
        CompletableFuture<Response> responseFuture = new CompletableFuture<>();
        Channel channel = manager.getChannel(serviceMetadata.getHost(), serviceMetadata.getPort());
        if (channel == null) {
            responseFuture.completeExceptionally(new RpcException("provider 获取失败"));
            return responseFuture;
        }
        inFlightRequests.put(request.getRequestId(), responseFuture);
        Timeout timeout = hashedWheelTimer.newTimeout(
                t -> responseFuture.completeExceptionally(new TimeoutException()),
                consumerProperties.getRequestTimeoutMs(),
                TimeUnit.MILLISECONDS);

        responseFuture.whenComplete((r, e) -> {
            inFlightRequests.remove(request.getRequestId());
            timeout.cancel();
        });
        channel.writeAndFlush(request).addListener(f -> {
            if (!f.isSuccess()) {
                responseFuture.completeExceptionally(f.cause());
            }
        });
        return responseFuture;
    }

    private Response tryRpcSync(Request request, ServiceMetadata serviceMetadata, long timeoutMs) throws ExecutionException, InterruptedException, TimeoutException {
        return tryRpcAsync(request, serviceMetadata).get(timeoutMs, TimeUnit.MILLISECONDS);
    }

    private class ConsumerHandler extends SimpleChannelInboundHandler<Response> {
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
