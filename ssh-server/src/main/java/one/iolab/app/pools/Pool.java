package one.iolab.app.pools;

import java.lang.ref.WeakReference;

public interface Pool {
    public static WeakReference<?>[] pool = null;

    public static PoolHolder<?, ?> poolHolder = null;
}
