import java.util.Arrays;
import java.util.Random;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.stream.Stream;

public class Reducer {
    public static int sum(int[] array) {
        /*var sum = 0;
        for (var value : array) {
            sum += value;
        }
        return sum;
         */
        return parallelReduceWithStream(array, 0, Integer::sum);
    }

    public static int max(int[] array) {
        /*var max = Integer.MIN_VALUE;
        for (var value : array) {
            max = Math.max(max, value);
        }
        return max;
         */
        return parallelReduceWithStream(array, Integer.MIN_VALUE, Math::max);
    }

    public static int reduce(int[] array, int initial, IntBinaryOperator op) {
        var acc = initial;
        for (var value : array) {
            acc = op.applyAsInt(acc, value);
        }
        return acc;
    }

    public static int reduceWithStream(int[] array, int initial, IntBinaryOperator op) {
        return Arrays.stream(array).reduce(initial, op);
    }

    public static int parallelReduceWithStream(int[] array, int initial, IntBinaryOperator op) {
        return Arrays.stream(array).parallel().reduce(initial, op);
    }

    private static class RecursiveTaskReduce extends RecursiveTask<Integer> {
        private int[] array;
        private int start;
        private int end;
        private IntBinaryOperator op;
        private int initial;

        public RecursiveTaskReduce(int[] array, int start, int end, int initial, IntBinaryOperator op) {
            this.array = array;
            this.start = start;
            this.end = end;
            this.op = op;
            this.initial = initial;
        }

        public static int parallelReduceWithForkJoin(int[] array, int initial, IntBinaryOperator op) {
            var pool = ForkJoinPool.commonPool();
            return pool.invoke(new RecursiveTaskReduce(array, 0, array.length, initial, op));
        }

        @Override
        protected Integer compute() {
            int born = end - start;
            if (born < 1024) {
                return Arrays.stream(array, start, end).reduce(initial, op);
            } else {
                int split = (end + start) / 2;
                var recursiveLeft = new RecursiveTaskReduce(array, start, split, initial, op);
                var recursiveRight = new RecursiveTaskReduce(array, split, end, initial, op);
                recursiveLeft.fork();
                var resultRight = recursiveRight.compute();
                var resultLeft = recursiveLeft.join();
                return op.applyAsInt(resultLeft, resultRight);
            }
        }
    }

    public static void main(String[] args) {

        var random = new Random(0); //le 0 c est pour indiquer que le random tire deux tableaux identiques
        int[] array = random.ints(1_000_000, 0, 1_000).toArray();
        System.out.println(max(array));
        System.out.println(sum(array));
        System.out.println(RecursiveTaskReduce.parallelReduceWithForkJoin(array, Integer.MIN_VALUE, Integer::max));
        System.out.println(RecursiveTaskReduce.parallelReduceWithForkJoin(array, 0, Integer::sum));
    }
}