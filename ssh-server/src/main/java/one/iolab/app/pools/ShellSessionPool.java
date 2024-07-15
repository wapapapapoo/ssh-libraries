package one.iolab.app.pools;

import java.lang.ref.WeakReference;

import one.iolab.app.adapters.AdapterImpl;
import one.iolab.app.config.Config;

public class ShellSessionPool implements Pool {
    @SuppressWarnings("unchecked")
    public static WeakReference<AdapterImpl<?>>[] pool = new WeakReference[Config.init.getInitShellPoolSize()];

    public static PoolHolder<ShellSessionPool, AdapterImpl<?>> poolHolder = null;
}
