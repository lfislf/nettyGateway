package io.github.lfislf.gateway.inbound;

import io.github.lfislf.gateway.outbound.HttpRequestHandler;
import io.github.lfislf.gateway.outbound.httpclient4.HttpClientOutboundHandler;
import io.github.lfislf.gateway.outbound.okhttp.OkhttpOutboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private final List<String> proxyServer;
    private HttpRequestHandler handler;

    public HttpInboundHandler(List<String> proxyServer) {
        this.proxyServer = proxyServer;
//        this.handler = new HttpClientOutboundHandler(this.proxyServer);
        this.handler = new OkhttpOutboundHandler(this.proxyServer);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            handler.handle(fullRequest, ctx);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }


}
