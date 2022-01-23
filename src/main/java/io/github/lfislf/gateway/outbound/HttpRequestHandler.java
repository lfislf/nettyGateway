package io.github.lfislf.gateway.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface HttpRequestHandler {
    void handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx);
}
