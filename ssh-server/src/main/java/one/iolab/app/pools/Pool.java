package one.iolab.app.pools;

public interface Pool<Elem_t> {
    public Elem_t[] getPool();

    public void setPool(Elem_t[] pool);

    public PoolHolder<? extends Pool<Elem_t>, Elem_t> getPoolHolder();

    public void setPoolHolder(PoolHolder<? extends Pool<Elem_t>, Elem_t> poolHolder);
}
