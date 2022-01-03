package com.imooc.flink.domain;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author jucheng
 * @since 2022/1/3
 */
public class AccessTest extends PojoTest<Access> {

    @Override
    protected String getJsonText() throws IOException {
        try (InputStream is = AccessTest.class.getResourceAsStream("/one-access-data.json")) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}
