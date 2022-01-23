package io.github.lfislf.gateway.filter;

import io.netty.handler.codec.http.FullHttpResponse;

public class MyHttpResponseFilter implements HttpResponseFilter {
    @Override
    public void filter(FullHttpResponse response) {
        response.headers().set("tag", "lf");
    }
}
