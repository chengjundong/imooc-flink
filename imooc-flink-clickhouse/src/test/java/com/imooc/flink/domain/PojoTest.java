package com.imooc.flink.domain;

import com.imooc.flink.exception.JaredFlinkException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.imooc.flink.exception.JaredFlinkErrorCode.TEST_ERROR;
import static com.imooc.flink.json.JacksonUtils.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author jared
 * @since 2022/1/3
 */
public abstract class PojoTest<E> {

    private final Class<E> clazz;

    public PojoTest() {
        final Type genericSuperclass = this.getClass().getGenericSuperclass();
        if (ParameterizedType.class.isInstance(genericSuperclass)) {
            this.clazz = Class.class.cast(
                    ParameterizedType.class.cast(genericSuperclass).getActualTypeArguments()[0]);
        } else {
            throw new JaredFlinkException(TEST_ERROR, "failed to get generic type of POJO");
        }
    }

    @Test
    public void test() throws Exception {
        final String jsonText = this.getJsonText();
        final E entity = fromJson(jsonText, clazz);
        final String result = toJson(entity);
        assertThat(result
                    .replace("\n", "")
                    .replace("\t", ""))
                .isEqualTo(jsonText);
    }

    protected abstract String getJsonText() throws IOException;
}
