package com.imooc.flink.parallel;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FilterOperator;
import org.apache.flink.util.NumberSequenceIterator;

public class ParallelismTest {

    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        DataSource<Long> source = env.fromParallelCollection(new NumberSequenceIterator(1, 100), Long.class)
                .setParallelism(2);

        System.out.println("source parallelism: " + source.getParallelism());

        FilterOperator<Long> filteredSource = source.filter(l -> l % 3 == 0);
        System.out.println("filtered source parallelism: " + filteredSource.getParallelism());
        // offline data, no need to call source.execute
        filteredSource.print();
    }
}
