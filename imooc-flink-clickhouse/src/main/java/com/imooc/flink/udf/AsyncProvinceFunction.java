package com.imooc.flink.udf;

import com.imooc.flink.domain.Access;
import com.imooc.flink.domain.IPInfo;
import org.apache.flink.api.java.tuple.Tuple3;

/**
 * To get province information in asynchronization mode
 *
 * @author jucheng
 * @since 2022/1/15
 */
public class AsyncProvinceFunction extends BaseAsyncIpMappingFunction<Tuple3<String, Integer, Long>> {

    @Override
    protected Tuple3<String, Integer, Long> map(IPInfo ipInfo, Access access) {
        return Tuple3.of(ipInfo.getProvince(), access.getNu(), 1L);
    }
}
