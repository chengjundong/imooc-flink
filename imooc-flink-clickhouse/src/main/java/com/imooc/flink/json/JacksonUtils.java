package com.imooc.flink.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.flink.exception.JaredFlinkException;

import java.io.InputStream;

import static com.imooc.flink.exception.JaredFlinkErrorCode.JSON_DESERIALIZATION_ERROR;
import static com.imooc.flink.exception.JaredFlinkErrorCode.JSON_SERIALIZATION_ERROR;

/**
 * @author jared
 * @since 2022/1/3
 */
public class JacksonUtils {

    private static final ObjectMapper JACKSON2 = new ObjectMapper();

    static {
        configureJson(JACKSON2);
    }

    /**
     * Add features into input jackson
     *
     * @param jackson2Mapper input jackson 2 mapper
     */
    public static void configureJson(ObjectMapper jackson2Mapper) {
        jackson2Mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        jackson2Mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <E> E fromJson(String jsonText, Class<E> entityClass) {
        try {
            return JACKSON2.readValue(jsonText, entityClass);
        } catch (Exception e) {
            throw new JaredFlinkException(JSON_DESERIALIZATION_ERROR, e.getMessage(), e);
        }
    }

    public static <E> E fromJson(InputStream is, Class<E> entityClass) {
        try {
            return JACKSON2.readValue(is, entityClass);
        } catch (Exception e) {
            throw new JaredFlinkException(JSON_DESERIALIZATION_ERROR, e.getMessage(), e);
        }
    }

    public static <E> E fromJson(String jsonText, TypeReference<E> entityType) {
        try {
            return JACKSON2.readValue(jsonText, entityType);
        } catch (Exception e) {
            throw new JaredFlinkException(JSON_DESERIALIZATION_ERROR, e.getMessage(), e);
        }
    }

    public static <E> E fromJson(InputStream is, TypeReference<E> entityType) {
        try {
            return JACKSON2.readValue(is, entityType);
        } catch (Exception e) {
            throw new JaredFlinkException(JSON_DESERIALIZATION_ERROR, e.getMessage(), e);
        }
    }

    public static <E> String toJson(E entity) {
        try {
            return JACKSON2.writeValueAsString(entity);
        } catch (Exception e) {
            throw new JaredFlinkException(JSON_SERIALIZATION_ERROR, e.getMessage(), e);
        }
    }
}
