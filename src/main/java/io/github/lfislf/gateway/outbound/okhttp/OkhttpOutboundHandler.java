package io.github.lfislf.gateway.outbound.okhttp;

import io.github.lfislf.gateway.filter.HttpRequestFilter;
import io.github.lfislf.gateway.filter.HttpResponseFilter;
import io.github.lfislf.gateway.filter.MyHttpRequestFilter;
import io.github.lfislf.gateway.filter.MyHttpResponseFilter;
import io.github.lfislf.gateway.outbound.HttpRequestHandler;
import io.github.lfislf.gateway.router.HttpEndpointRouter;
import io.github.lfislf.gateway.router.RandomHttpEndpointRouter;
import io.github.lfislf.gateway.threadpool.ExecutorUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class OkhttpOutboundHandler implements HttpRequestHandler {

    HttpRequestFilter requestFilter = new MyHttpRequestFilter();
    HttpResponseFilter responseFilter = new MyHttpResponseFilter();
    HttpEndpointRouter router = new RandomHttpEndpointRouter();

    private ExecutorService proxyService;
    private List<String> backendUrls;

    private OkHttpClient okHttpClient;


    public OkhttpOutboundHandler(List<String> backends) {
        backendUrls = backends.stream().map(this::formatUrl).collect(Collectors.toList());
        proxyService = ExecutorUtil.createExecutorService("proxyService");
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1000, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public void handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        String backendUrl = router.route(this.backendUrls);
        System.out.println(backendUrl);
        final String url = backendUrl + fullRequest.uri();
        requestFilter.filter(fullRequest, ctx);
        proxyService.submit(() -> handleResponse(ctx, fullRequest, url));
    }

    private void handleResponse(ChannelHandlerContext ctx, FullHttpRequest fullRequest, String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("gateway", fullRequest.headers().get("gateway"))
                    .build();
            Response resp = okHttpClient.newCall(request).execute();
            byte[] bytes = resp.body().bytes();

            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(bytes));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", Integer.parseInt(resp.header("Content-Length")));

            responseFilter.filter(response);
            ctx.writeAndFlush(response);
        } catch (IOException e) {
            e.printStackTrace();
            ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR));
        }
    }

    private String formatUrl(String backend) {
        return backend.endsWith("/") ? backend.substring(0, backend.length() - 1) : backend;
    }
}
