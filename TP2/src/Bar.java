import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class Bar {
    private volatile int i;
    private final static VarHandle VH_BAR_FIELD_I;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            VH_BAR_FIELD_I = lookup.findVarHandle(Bar.class, "i", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    public int nextValue() {
        for (; ; ) {
            int current = this.i;
            if (VH_BAR_FIELD_I.compareAndSet(this, current, current + 1)) {
                return current;
            }
        }
    }
}
