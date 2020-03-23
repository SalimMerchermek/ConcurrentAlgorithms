import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class COWSet<E> {
    private final E[][] hashArray;

    private static final Object[] EMPTY = new Object[0];

    private static final VarHandle ARRAY_HANDLE;

    static {
        ARRAY_HANDLE = MethodHandles.arrayElementVarHandle(Object[][].class);
    }

    @SuppressWarnings("unchecked")
    public COWSet(int capacity) {
        var array = new Object[capacity][];
        Arrays.setAll(array, __ -> EMPTY);
        this.hashArray = (E[][]) array;
    }

    @SuppressWarnings("unchecked")
    public boolean add(E element) {
        Objects.requireNonNull(element);
        var index = element.hashCode() % hashArray.length;

        while (true) {
            var oldArray = (E[]) ARRAY_HANDLE.getVolatile(hashArray, index);
            for (var e : oldArray) { // il faut llire de facon volatile utiliser vh.getVolatile (hashArray, index) 1
                if (element.equals(e)) {
                    return false;
                }
            }
            //Entre 1 et 2 il faut faire une seule lecture

            var newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
            newArray[oldArray.length] = element;
            if (ARRAY_HANDLE.compareAndSet(hashArray, index, oldArray, newArray)) {
                return true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void forEach(Consumer<? super E> consumer) {
        for (var index = 0; index < hashArray.length; index++) {
            // il faut llire de facon volatile utiliser vh.getVolatile (hashArray, index)
            var oldArray = (E[]) ARRAY_HANDLE.getVolatile(hashArray, index);
            for (var element : oldArray) {
                consumer.accept(element);
            }
        }
    }
}