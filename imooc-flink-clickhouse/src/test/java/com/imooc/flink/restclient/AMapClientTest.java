package com.imooc.flink.restclient;

import com.imooc.flink.domain.IPInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author jucheng
 * @since 2022/1/6
 */
public class AMapClientTest {

    private final AMapClient client = new AMapClient();

    /**
     * <pre>
     * {
     *     "status": "1",
     *     "info": "OK",
     *     "infocode": "10000",
     *     "country": "中国",
     *     "province": "北京市",
     *     "city": "北京市",
     *     "district": "朝阳区",
     *     "isp": "中国联通",
     *     "location": "116.486409,39.921489",
     * }
     * </pre>
     *
     */
    @Test
    public void get_IP_SuccessResponse() {
        String url = "https://restapi.amap.com/v5/ip?ip=114.247.50.2&key=60bc95cdcb4bd227934ef81899e2df8c&type=4";

        final IPInfo ipInfo = client.get(url, IPInfo.class);

        assertThat(ipInfo).isNotNull()
                .extracting(IPInfo::getStatus, IPInfo::getCountry, IPInfo::getProvince, IPInfo::getCity, IPInfo::getDistrict, IPInfo::getIsp)
                .containsExactly("1", "中国", "北京市", "北京市", "朝阳区", "中国联通");
    }
}
