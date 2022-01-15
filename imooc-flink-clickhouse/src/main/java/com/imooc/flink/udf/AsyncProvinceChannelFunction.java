package com.imooc.flink.udf;

import com.imooc.flink.domain.Access;
import com.imooc.flink.domain.IPInfo;
import org.apache.flink.api.java.tuple.Tuple4;

/**
 * Map access & ip to channel & province combination
 *
 * @author jucheng
 * @since 2022/1/15
 */
public class AsyncProvinceChannelFunction extends BaseAsyncIpMappingFunction<Tuple4<String, String, Integer, Long>> {

    @Override
    protected Tuple4<String, String, Integer, Long> map(IPInfo ipInfo, Access access) {
        return Tuple4.of(ipInfo.getProvince(), access.getChannel(), access.getNu(), 1L);
    }
}
