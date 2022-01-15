package com.imooc.flink.udf;

import com.imooc.flink.domain.Access;
import com.imooc.flink.domain.IPInfo;
import com.imooc.flink.restclient.AMapClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * To parse IP and map data to Tuple in asynchronization mode
 *
 * @author jucheng
 * @since 2022/1/15
 */
public abstract class BaseAsyncIpMappingFunction<T> extends RichAsyncFunction<Access, T> {

    private AMapClient aMapClient;

    @Override
    public void open(Configuration parameters) throws Exception {
        aMapClient = new AMapClient();
    }

    @Override
    public void close() throws Exception {
        aMapClient = null;
    }

    @Override
    public void asyncInvoke(Access access, ResultFuture<T> resultFuture) throws Exception {
        String url = String.format("https://restapi.amap.com/v5/ip?ip=%s&key=60bc95cdcb4bd227934ef81899e2df8c&type=4", access.getIp());
        final CompletableFuture<HttpResponse<String>> future = aMapClient.getAsync(url);

        CompletableFuture.<IPInfo>supplyAsync(() -> {
            try {
                final HttpResponse<String> response = future.get();
                return aMapClient.getEntity(response, IPInfo.class);
            } catch (Exception e) {
                return null;
            }
        }).thenAccept(ipInfo -> {
            if(null != ipInfo) {
                resultFuture.complete(Collections.singleton(this.map(ipInfo, access)));
            }
        });
    }

    protected abstract T map(IPInfo ipInfo, Access access);
}
