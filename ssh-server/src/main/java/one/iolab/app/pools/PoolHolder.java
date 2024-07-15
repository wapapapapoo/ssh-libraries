package one.iolab.app.pools;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class PoolHolder<T extends Pool, K> {
    // private Class<T> poolClass;
    private Field poolField;
    private WeakReference<K>[] pool;

    private AtomicBoolean flushLock = new AtomicBoolean(false);

    @SuppressWarnings("unchecked")
    public PoolHolder(Class<T> poolClass) {
        // this.poolClass = poolClass;
        try {
            this.poolField = poolClass.getField("pool");
            if (this.poolField != null) {
                Object poolObj = this.poolField.get(null);
                if (poolObj instanceof WeakReference<?>[]) {
                    this.pool = (WeakReference<K>[]) poolObj;
                }
            }

            poolClass.getField("poolHolder").set(null, this);
        } catch (ReflectiveOperationException e) {
        }
    }

    public void expand(int to) {
        assert to > this.pool.length;
        this.pool = Arrays.copyOf(this.pool, to);
        try {
            poolField.set(null, this.pool);
        } catch (ReflectiveOperationException e) {
        }
    }

    public synchronized int flush() {

        int sum = 0;

        if (!this.flushLock.compareAndSet(false, true)) {
            for (int i = 0; i < this.pool.length; i++) {
                if (this.pool[i] != null && this.pool[i].get() != null) {
                    sum++;
                    continue;
                }
            }

            return sum;
        }

        try {
            System.gc();

            for (int i = 0; i < this.pool.length; i++) {
                if (this.pool[i] == null) {
                    continue;
                }

                if (this.pool[i].get() != null) { // 槽位非空
                    sum++;
                    continue;
                }

                if (this.pool[i].get() == null) {
                    this.pool[i] = null;
                }
            }
        } finally {
            flushLock.set(false);
        }

        return sum;
    }

    public int firstFreeSlot() {
        for (int i = 0; i < this.pool.length; i++) {
            if (this.pool[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public void put(int index, K value) {
        this.pool[index] = new WeakReference<K>(value);
    }
}
