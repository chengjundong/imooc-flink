package com.imooc.flink.restclient;

import com.imooc.flink.exception.JaredFlinkErrorCode;
import com.imooc.flink.exception.JaredFlinkException;
import com.imooc.flink.json.JacksonUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * REST API client to call AMap API
 *
 * @author jucheng
 * @since 2022/1/6
 * @see <a href="https://lbs.amap.com/api/webservice/guide/api/ipconfig">AMAP IP Config API</a>
 */
public class AMapClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    /**
     *
     * @param url absolute URL
     * @param entityClass class to de-serialize response
     * @param <E> generic type of entity class
     * @return an instance of response in entity class type
     */
    public <E> E get(String url, Class<E> entityClass) {
        try {
            final HttpRequest httpRequest = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .build();
            final HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if(response.statusCode() / 100 == 2) {
                return JacksonUtils.fromJson(response.body(), entityClass);
            } else {
                throw new JaredFlinkException(JaredFlinkErrorCode.HTTP_CLIENT_ERROR, response.body());
            }
        } catch (Exception e) {
            throw new JaredFlinkException(JaredFlinkErrorCode.HTTP_CLIENT_ERROR, e.getMessage(), e);
        }
    }
}
