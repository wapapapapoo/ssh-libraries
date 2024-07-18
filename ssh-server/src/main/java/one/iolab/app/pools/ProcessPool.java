package one.iolab.app.pools;

import org.apache.sshd.server.channel.ChannelSession;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import one.iolab.app.adapters.Adapter;
import one.iolab.app.config.Config;
import one.iolab.app.drivers.BaseDriver;

public class ProcessPool implements Pool<ProcessPool.Record> {

    private static Record[] pool = new Record[Config.init.getInitPoolSize()];

    private static PoolHolder<? extends Pool<Record>, Record> poolHolder = new PoolHolder<ProcessPool, Record>(
            new ProcessPool());

    public void setPool(ProcessPool.Record[] pool) {
        ProcessPool.pool = pool;
    }

    public ProcessPool.Record[] getPool() {
        return pool;
    }

    public PoolHolder<? extends Pool<Record>, Record> getPoolHolder() {
        return poolHolder;
    }

    public void setPoolHolder(PoolHolder<? extends Pool<Record>, Record> poolHolder) {
        ProcessPool.poolHolder = poolHolder;
    }

    @Data
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Record {

        public int processId;
        public ChannelSession channel;
        public Adapter adapter;
        public Class<? extends BaseDriver<?>> driver;

    }
}
