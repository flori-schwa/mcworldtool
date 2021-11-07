package me.shawlaf.mcworldtool.util;

import lombok.experimental.UtilityClass;

import java.util.function.IntFunction;

@UtilityClass
public class MultithreadUtil {

    public <T> T[][] splitTaskForNThreads(Class<T> type, int threads, T[] data, IntFunction<T[]> arrayCreator, IntFunction<T[][]> arrayCreator2d) {
        if (threads < 1) {
            throw new IllegalArgumentException("Amount of threads must be >= 1");
        }

        T[][] result = arrayCreator2d.apply(threads);

        int threadBatchSize = data.length / threads;
        int remainder = data.length % threads;

        for (int i = 0; i < threads; i++) {
            if (i == (threads - 1)) {
                result[i] = arrayCreator.apply(threadBatchSize + remainder);
            } else {
                result[i] = arrayCreator.apply(threadBatchSize);
            }

            System.arraycopy(data, threadBatchSize * i, result[i], 0, result[i].length);
        }

        return result;
    }

}
