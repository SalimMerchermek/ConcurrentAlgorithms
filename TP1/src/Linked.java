import java.util.ArrayList;

// size peut valoir 0 tout le temps
//
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Linked<E> {
    private static class Entry<E> {
        private final E element;
        private final Entry<E> next;

        private Entry(E element, Entry<E> next) {
            this.element = element;
            this.next = next;
        }
    }

    private final AtomicReference<Entry<E>> head = new AtomicReference<>();

    public void addFirst(E element) {
        Objects.requireNonNull(element);

        while (true) {
            var currentHead = head.get() ;
            var newHead =  new Entry<>(element, currentHead) ;
            if (head.compareAndSet(currentHead, newHead)) return;
        }
    }

    public int size() {
        var size = 0;
        for(var link = head.get(); link != null; link = link.next) {
            size ++;
        }
        return size;
    }

    public static void main(String[] args) throws InterruptedException {
        var list = new Linked<Integer>() ;
        var threads = new ArrayList<Thread> ();

        for (var i=0; i<2; i++) {
            var thread = new Thread(() -> {
                for (var j = 0; j < 100; j++) list.addFirst(3);
            });
            thread.start();
            threads.add(thread);
        }
        for (var thread: threads) {
            thread.join();
        }

        System.out.println(list.size());

    }
}
