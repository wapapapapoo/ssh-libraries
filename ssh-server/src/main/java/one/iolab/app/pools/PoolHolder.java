package one.iolab.app.pools;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class PoolHolder<Pool_t extends Pool<Elem_t>, Elem_t> implements Iterable<Elem_t> {
    private Pool<Elem_t> pool;

    private AtomicBoolean flushLock = new AtomicBoolean(false);

    public PoolHolder(Pool_t pool) {
        this.pool = pool;
        pool.setPoolHolder(this);
    }

    public void expand(int to) {
        assert to > this.pool.getPool().length;
        Elem_t[] newPool = Arrays.copyOf(this.pool.getPool(), to);
        pool.setPool(newPool);
    }

    private class PoolIterator implements Iterator<Elem_t> {
        private boolean end = true;
        private int index = 0;
        private Elem_t[] pool;

        private void moveToNext() {
            this.end = true;
            for (int i = this.index; i < this.pool.length; i++) {
                if (this.pool[i] != null) {
                    this.end = false;
                    this.index = i;
                    return;
                }
            }
        }

        public void init(Elem_t[] pool) {
            this.pool = pool;
            assert pool != null;
            moveToNext();
        }

        @Override
        public boolean hasNext() {
            if (this.pool == null)
                return false;
            return !this.end;
        }

        @Override
        public Elem_t next() {
            if (this.pool == null)
                return null;
            if (this.end)
                return null;
            Elem_t value = this.pool[this.index];
            this.index++;
            moveToNext();
            return value;
        }
    }

    public Iterator<Elem_t> iterator() {
        PoolIterator iter = new PoolIterator();
        iter.init(this.pool.getPool());
        return iter;
    }

    @FunctionalInterface
    public interface GCLambda<Elem_t> {
        boolean mark(Elem_t pool);
    }

    public synchronized int flush(GCLambda<Elem_t> gc) {

        if (gc == null) {
            gc = (Elem_t pool) -> {
                return false;
            };
        }

        int sum = 0;
        Elem_t[] pool = this.pool.getPool();

        if (!this.flushLock.compareAndSet(false, true)) {
            for (int i = 0; i < pool.length; i++) {
                if (pool[i] != null) {
                    sum++;
                }
            }

            return sum;
        }

        try {
            for (int i = 0; i < pool.length; i++) {
                if (pool[i] != null) { // 槽位非空
                    if (gc.mark(pool[i])) {
                        pool[i] = null;
                    } else {
                        sum++;
                    }
                }
            }

            System.gc();
        } finally {
            flushLock.set(false);
        }

        return sum;
    }

    public int firstFreeSlot() {
        Elem_t[] pool = this.pool.getPool();

        for (int i = 0; i < pool.length; i++) {
            if (pool[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public void put(int index, Elem_t value) {

        this.pool.getPool()[index] = value;
    }

    public Elem_t get(int index) {
        return this.pool.getPool()[index];
    }
}
