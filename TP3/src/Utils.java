import jdk.jshell.execution.Util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.file.Path;
import java.time.temporal.ValueRange;

public class Utils {
    /* private static Path HOME;
     private static final VarHandle HANDLE;

     static {
         var lookup = MethodHandles.lookup();
         try {
             HANDLE = lookup.findStaticVarHandle(Utils.class, "HOME", Path.class);
         } catch (NoSuchFieldException | IllegalAccessException e) {
             throw new AssertionError(e);
         }
     }

     public static Path getHome() {
         var home = (Path) HANDLE.getAcquire();
         if (home == null) {
             synchronized (Utils.class) {
                 home = (Path) HANDLE.getAcquire();
                 if (home == null) {
                     HANDLE.setRelease(Path.of(System.getenv("HOME")));
                     return (Path) HANDLE.getAcquire();
                 }
             }
         }
         return home;
     }
  */


    private static class lazyHolder {
        static final Path HOME = Path.of(System.getenv("HOME"));
    }

    public static Path getHome() {
        return lazyHolder.HOME;
    }

}




