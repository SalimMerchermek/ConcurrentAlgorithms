import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.logging.Handler;

public class SpinLock {
    private volatile boolean lock;
    private static final VarHandle HANDLE;

    static {
        var lookup = MethodHandles.lookup();
        try {
            HANDLE = lookup.findVarHandle(SpinLock.class, "lock", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public void lock() {
        while (!HANDLE.compareAndSet(this, false, true)) {
            Thread.onSpinWait();
        }
    }

    public boolean tryLock() {
      return HANDLE.compareAndSet(this, false, true);
    }

    public void unlock() {
        lock = false;// on peut faire ça, car lock est volatile
    }

    public static void main(String[] args) throws InterruptedException {
        var runnable = new Runnable() {
            private int counter;
            private final SpinLock spinLock = new SpinLock();

            @Override
            public void run() {
                for (int i = 0; i < 1_000_000; i++) {
                    spinLock.lock();
                    try {
                        counter++; // 3 opérations, donc pas thread safe meme si le champ est volatile
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
