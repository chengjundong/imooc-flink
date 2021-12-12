package com.imooc.flink.partitioner;

import org.apache.flink.api.common.functions.Partitioner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jucheng
 * @since 2021/12/13
 */
public class WordCountPartitioner implements Partitioner<String> {

    private static Map<String, Integer> PARTITION = new HashMap<>() {
        {
            this.put("java", 1);
            this.put("python", 2);
            this.put("c++", 5);
            this.put("golang", 3);
            this.put("PHP", 4);
        }
    };

    @Override
    public int partition(String key, int numPartitions) {
        return PARTITION.getOrDefault(key, 0);
    }
}
