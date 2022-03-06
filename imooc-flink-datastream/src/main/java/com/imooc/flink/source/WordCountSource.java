package com.imooc.flink.source;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author jared
 * @since 2021/12/11
 */
public class WordCountSource implements SourceFunction<String> {

    private boolean running = true;

    private final String[] words = new String[]{"java", "python", "c++", "golang", "PHP", "scala"};

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        for (; running; ) {
            int index = ThreadLocalRandom.current().nextInt(0, 6);
            ctx.collect(words[index]);
        }
    }

    @Override
    public void cancel() {
        this.running = false;
    }
}
