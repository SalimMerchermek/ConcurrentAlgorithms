import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LockFreeStringList {
    static final class Entry {
        final String element;
        volatile Entry next;

        Entry(String element) {
            this.element = element;
        }
    }

    private final Entry head;
    private static final VarHandle Next_Handle, Tail_Handle;
    private volatile Entry tail;

    static {
        var lookup = MethodHandles.lookup();
        try {
            Next_Handle = lookup.findVarHandle(Entry.class, "next", Entry.class);
            Tail_Handle = lookup.findVarHandle(LockFreeStringList.class, "tail", Entry.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public LockFreeStringList() {
        tail = head = new Entry(null); // fake first entry
    }

    public void addLast(String element) {
        var entry = new Entry(element);
        var oldTail = tail;
        var last = oldTail;
        for (; ; ) {
            var next = last.next;
            if (next == null) {
                if (Next_Handle.compareAndSet(last, null, entry)) {
                    Tail_Handle.compareAndSet(this, oldTail, entry);
                    return;
                }
                next = tail;
            }
            last = next;
        }
    }

    public int size() {
        var count = 0;
        for (var e = head.next; e != null; e = e.next) {
            count++;
        }
        return count;
    }

    private static Runnable createRunnable(LockFreeStringList list, int id) {
        return () -> {
            for (var j = 0; j < 10_000; j++) {
                list.addLast(id + " " + j);
            }
        };
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        var threadCount = 5;
        var list = new LockFreeStringList();
        var tasks = IntStream.range(0, threadCount)
                .mapToObj(id -> createRunnable(list, id))
                .map(Executors::callable)
                .collect(Collectors.toList());
        var executor = Executors.newFixedThreadPool(threadCount);
        var futures = executor.invokeAll(tasks);
        executor.shutdown();
        for (var future : futures) {
            future.get();
        }
        System.out.println(list.size());
    }
}