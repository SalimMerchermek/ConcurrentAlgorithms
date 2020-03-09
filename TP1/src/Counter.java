import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private final AtomicInteger counter = new AtomicInteger();

    public int nextInt() {
        while (true) {
            var currentValue = counter.get();
            if (counter.compareAndSet(currentValue, currentValue+1)) return currentValue  ;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        var counter = new Counter();
        var threads = new ArrayList<Thread> ();
        for (var i=0; i<4; i++) {
           var thread = new Thread(() -> {
                for (var j=0; j<10000; j++) {
                    counter.nextInt();
                }
            });
           thread.start();
           threads.add(thread);
        }

        for (var thread: threads) {
            thread.join();
        }

        System.out.println(counter.counter);

    }
}
