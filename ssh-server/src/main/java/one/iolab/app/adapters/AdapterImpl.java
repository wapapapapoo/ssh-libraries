package one.iolab.app.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import one.iolab.app.drivers.BaseDriver;
import one.iolab.app.iostreamwrapper.ByteConsumerImpl;
import one.iolab.app.iostreamwrapper.ByteConsumerNullImpl;
import one.iolab.app.iostreamwrapper.ByteProviderImpl;
import one.iolab.app.iostreamwrapper.ByteProviderNullImpl;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;

public final class AdapterImpl<T extends BaseDriver> implements Adapter {

    private Adapter.State state = Adapter.State.NULL;

    public Adapter.State getState() {
        return this.state;
    }

    public void setState(Adapter.State state) {
        this.state = state;
    }

    private String driverName;
    private Number processId;

    private Class<T> driver;
    private BaseDriver driverInstance;

    private Map<String, Object> argv;

    private InputStream in = null;
    private OutputStream out = null, err = null;
    private ExitCallback callback;
    private ChannelSession channel;
    private Environment env;

    private Thread thread;
    private Logger logger;

    private BaseDriver generateDriverInstance() {
        try {
            BaseDriver instance = (BaseDriver) this.driver
                    .getDeclaredConstructor()
                    .newInstance();

            instance.setProcessId(this.processId);
            instance.setArgv(this.argv);
            instance.setCin(this.in != null ? new AdapterByteProvider(this.in) : new ByteProviderNullImpl());
            instance.setCout(this.out != null ? new AdapterByteConsumer(this.out) : new ByteConsumerNullImpl());
            instance.setCerr(this.err != null ? new AdapterByteConsumer(this.err) : new ByteConsumerNullImpl());
            instance.setThread(this.thread);
            instance.setEnv(this.env);
            instance.setChannel(this.channel);
            instance.setAdapter(this);
            instance.setExitCallback(this.callback);

            return instance;
        } catch (ReflectiveOperationException e) {
            err("fail to load driver", e);
            this.callback.onExit(0, "exited", true);
        }

        return null;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        this.channel = channel;
        this.env = env;

        this.driverInstance = this.generateDriverInstance();

        this.driverName = this.driverInstance.getDriverName();
        String threadName = String.format("%s-%d", this.driverName, this.processId);

        this.thread = new Thread(this.driverInstance);
        this.thread.setName(threadName);
        this.thread.setDaemon(false);
        this.thread.setUncaughtExceptionHandler((t, e) -> {
            this.state = State.INTED;
            err(String.format("uncaught exception in thread <%s>", threadName), e);
        });

        this.state = State.RUNNING;
        this.thread.start();
    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        channel.close();
        this.state = State.DESTROY; // 4 destroyed
    }

    public void shutdown() {
        try {
            this.driverInstance.shutdown(); // callback
            this.thread.interrupt();
        } catch (SecurityException e) {
            // do sth...
        } finally {
            this.state = State.INTED; // 3 exception occured
        }
    }

    public AdapterImpl(
            Number processId,
            Class<T> driver,
            Map<String, Object> argv) {

        this.driver = driver;
        this.processId = processId;
        this.argv = argv != null ? argv : new HashMap<String, Object>();
        this.logger = org.slf4j.LoggerFactory.getLogger(AdapterImpl.class);
        this.state = State.READY; // 1 constructed
    }

    private void err(String msg, Throwable e) {
        this.logger.error(String.format("%s-%d: %s", this.driverName, this.processId, msg), e);
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }
}

class AdapterByteProvider extends ByteProviderImpl {

    AdapterByteProvider(InputStream in) {
        super(in);
    }

}

class AdapterByteConsumer extends ByteConsumerImpl {

    AdapterByteConsumer(OutputStream out) {
        super(out);
    }

}
