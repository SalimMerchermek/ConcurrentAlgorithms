import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ForkJoinCollections {

    private static class RecursiveTaskReduce<T, V> extends RecursiveTask<V> {
        private Spliterator<T> spliterator;
        private int threshold;
        private V initialValue;
        private BiFunction<T, V, V> accumulator;
        private BinaryOperator<V> combiner;

        public RecursiveTaskReduce(Spliterator<T> spliterator,
                                   int threshold,
                                   V initialValue,
                                   BiFunction<T, V, V> accumulator,
                                   BinaryOperator<V> combiner) {
            this.spliterator = spliterator;
            this.threshold = threshold;
            this.initialValue = initialValue;
            this.accumulator = accumulator;
            this.combiner = combiner;
        }

        @Override
        protected V compute() {
            if (spliterator.estimateSize() < threshold)
                return sequentialReduce(spliterator, initialValue, accumulator);
            else {
                var spliterator2 = spliterator.trySplit();
                var recursiveLeft = new RecursiveTaskReduce<>(spliterator, threshold, initialValue, accumulator, combiner);
                var recursiveRight = new RecursiveTaskReduce<>(spliterator2, threshold, initialValue, accumulator, combiner);
                recursiveLeft.fork();
                var resultRight = recursiveRight.compute();
                var resultLeft = recursiveLeft.join();
                return combiner.apply(resultLeft, resultRight);
            }
        }
    }


    public static <T, V> V sequentialReduce(Spliterator<T> spliterator, V initial, BiFunction<T, V, V> op) {
        var box = new Object() { // class anonyme
            private V acc = initial;
        };

        while (spliterator.tryAdvance(e -> box.acc = op.apply(e, box.acc))) ;
        return box.acc;
    }

    public static <V, T> V forkJoinReduce(Collection<T> collection, int threshold, V initialValue,
                                          BiFunction<T, V, V> accumulator, BinaryOperator<V> combiner) {

        return forkJoinReduce(collection.spliterator(), threshold, initialValue, accumulator, combiner);
    }

    private static <V, T> V forkJoinReduce(Spliterator<T> spliterator, int threshold, V initialValue,
                                           BiFunction<T, V, V> accumulator, BinaryOperator<V> combiner) {
        var pool = ForkJoinPool.commonPool();
        return pool.invoke(new RecursiveTaskReduce<>(spliterator, threshold, initialValue, accumulator, combiner));

    }

    public static void main(String[] args) {
        // sequential
        System.out.println(IntStream.range(0, 10_000).sum());

        // fork/join
        var list = IntStream.range(0, 10_000).boxed().collect(Collectors.toList());
        var result = forkJoinReduce(list, 1_000, 0, (acc, value) -> acc + value, (acc1, acc2) -> acc1 + acc2);
        System.out.println(result);
    }


}