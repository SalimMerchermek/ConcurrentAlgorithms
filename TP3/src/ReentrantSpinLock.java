import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class ReentrantSpinLock {
    private volatile int lock;
    private Thread ownerThread;
    private static final VarHandle HANDLE;

    static {
        var lookup = MethodHandles.lookup();
        try {
            HANDLE = lookup.findVarHandle(ReentrantSpinLock.class, "lock", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public void lock() {
        var current = Thread.currentThread();
        for (; ; ) {
            if (HANDLE.compareAndSet(this, 0, 1)) {
                ownerThread = current;
                return;
            }
            if (ownerThread == current) {
                lock++;
                return;
            }
            Thread.onSpinWait();
        }
    }

    public void unlock() {
        if (ownerThread != Thread.currentThread()) {
            throw new IllegalStateException();
        }
        var lock = this.lock;
        if (lock == 1) {
            ownerThread = null;
            this.lock = 0; // on peut ne pas mettre ownerThread comme volatile car lock est colatile
            // et donc toutes les ecritures avant sont assurées en ram
            return;
        }
        this.lock = lock - 1;
    }

    public static void main(String[] args) throws InterruptedException {
        var runnable = new Runnable() {
            private int counter;
            private final ReentrantSpinLock spinLock = new ReentrantSpinLock();

            @Override
            public void run() {
                for (var i = 0; i < 1_000_000; i++) {
                    spinLock.lock();
                    try {
                        spinLock.lock();
                        try {
                            counter++;
                        } finally {
                            spinLock.unlock();
                        }
                    } finally {
                        spinLock.unlock();
                    }
                }
            }
        };
        var t1 = new Thread(runnable);
        var t2 = new Thread(runnable);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("counter " + runnable.counter);
    }
}