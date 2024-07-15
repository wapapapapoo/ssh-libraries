package one.iolab.app.pools;

import java.lang.ref.WeakReference;

import one.iolab.app.adapters.AdapterImpl;
import one.iolab.app.config.Config;

public class SCPSessionPool implements Pool {
    @SuppressWarnings("unchecked")
    public static WeakReference<AdapterImpl<?>>[] pool = new WeakReference[Config.init.getInitScpPoolSize()];

    public static PoolHolder<SCPSessionPool, AdapterImpl<?>> poolHolder = null;
}
