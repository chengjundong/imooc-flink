package com.imooc.flink.udf;

import com.imooc.flink.domain.Access;
import com.imooc.flink.domain.IPInfo;
import com.imooc.flink.restclient.AMapClient;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * To get province information in asynchronization mode
 *
 * @author jucheng
 * @since 2022/1/15
 */
public class AsyncProvinceFunction extends RichAsyncFunction<Access, Tuple3<String, Integer, Long>> {

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
    public void asyncInvoke(Access access, ResultFuture<Tuple3<String, Integer, Long>> resultFuture) throws Exception {
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
                resultFuture.complete(Collections.singleton(Tuple3.of(ipInfo.getProvince(), access.getNu(), 1L)));
            }
        });
    }
}
