package one.iolab.app.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.iolab.app.drivers.BaseDriver;
import one.iolab.app.iostreamwrapper.ByteConsumerImpl;
import one.iolab.app.iostreamwrapper.ByteConsumerNullImpl;
import one.iolab.app.iostreamwrapper.ByteProviderImpl;
import one.iolab.app.iostreamwrapper.ByteProviderNullImpl;

import org.apache.sshd.common.SshConstants;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.session.ServerSession;

public final class AdapterImpl<Driver_t extends BaseDriver<Arg_t>, Arg_t> implements Adapter {

    /* state */

    private Adapter.State state = Adapter.State.NULL;

    public Adapter.State getState() {
        return this.state;
    }

    public void setState(Adapter.State state) {
        this.state = state;
    }

    /* fields */

    private String driverName;
    private Number processId;

    private Class<Driver_t> driver;
    private BaseDriver<Arg_t> driverInstance;

    private Arg_t argv;

    private InputStream in = null;
    private OutputStream out = null, err = null;
    private ExitCallback exitCallback;
    private ChannelSession channel;
    private Environment env;

    private Thread thread;
    private Logger logger;

    /* callbacks */

    @FunctionalInterface
    private interface InterruptCallback {
        public boolean fn(int state, Object arg);
    }

    @FunctionalInterface
    private interface UncaughtExceptionCallback {
        public void fn(Thread t, Throwable e);
    }

    InterruptCallback interruptHandler = (int state, Object arg) -> {
        return true;
    };

    UncaughtExceptionCallback uncaughtExceptionHandler = (Thread t, Throwable e) -> {
        this.logger.error("Uncaught Exception Occured:", e);
    };

    public class Callback implements ExitCallback {

        AdapterImpl<Driver_t, Arg_t> adapter = null;

        Callback(AdapterImpl<Driver_t, Arg_t> adapter) {
            this.adapter = adapter;
        }

        public void onExit(int exitValue, String exitMessage, boolean closeImmediately) {
            this.adapter.exitCallback.onExit(exitValue, exitMessage, closeImmediately);
        }

        public void raise(int signal, Object arg) {
            this.adapter.interrupt(signal, arg);
        }

        public void setInterruptHandler(InterruptCallback fn) {
            this.adapter.interruptHandler = fn;
        }

        public void setUncaughtExceptionHandler(UncaughtExceptionCallback fn) {
            this.adapter.uncaughtExceptionHandler = fn;
        }

    }

    /* api */

    public void interrupt(int signal, Object args) {
        this.setState(Adapter.State.INTED);

        String reason = args != null ? args.toString() : "Rcev int " + Integer.valueOf(signal).toString();

        // 不使用默认处理
        if (this.interruptHandler != null
                && signal != Adapter.Signal.SIGKILL
                && !this.interruptHandler.fn(signal, args))
            return;

        try {
            try {
                if (this.thread != null)
                    this.thread.interrupt();
            } catch (SecurityException e) {
            }

            this.exitCallback.onExit(signal, reason, true);

            try {
                this.channel.getSession().disconnect(SshConstants.SSH2_DISCONNECT_BY_APPLICATION, reason);
            } catch (IOException e) {
                this.channel.getSession().close(true);
            }

            try {
                this.channel.close();
            } catch (IOException e) {
            }

        } catch (Exception e) { // 漏网之鱼
            this.logger.error("unhandled exception appeared in AdapterImpl.interrupt", e);
        }

        this.setState(Adapter.State.STOPED);

    }

    public ChannelSession getChannel() {
        return this.channel;
    }

    public ServerSession getSession() {
        return this.channel.getSession();
    }

    /* impl */

    private BaseDriver<Arg_t> generateDriverInstance() {
        try {
            BaseDriver<Arg_t> instance = (BaseDriver<Arg_t>) this.driver
                    .getDeclaredConstructor()
                    .newInstance();

            instance.setProcessId(this.processId);
            instance.setArgv(this.argv);
            instance.setCin(this.in != null ? new AdapterByteProvider(this.in) : new ByteProviderNullImpl());
            instance.setCout(this.out != null ? new AdapterByteConsumer(this.out) : new ByteConsumerNullImpl());
            instance.setCerr(this.err != null ? new AdapterByteConsumer(this.err) : new ByteConsumerNullImpl());
            instance.setEnv(this.env);
            instance.setChannel(this.channel);
            instance.setAdapter(this);
            instance.setCallback(new Callback(this));
            instance.setLogger(LoggerFactory.getLogger(instance.getClass()));

            return instance;
        } catch (ReflectiveOperationException e) {
            this.interrupt(Adapter.Signal.SIGILL, e); // sigill
        }

        return null;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        this.channel = channel;
        this.env = env;

        this.driverInstance = this.generateDriverInstance();
        if (driverInstance == null)
            return;

        this.driverName = Driver_t.driverName;
        String threadName = String.format("%s-%d", this.driverName, this.processId);

        this.thread = new Thread(this.driverInstance);
        this.thread.setName(threadName);
        this.thread.setDaemon(false);
        this.thread.setUncaughtExceptionHandler((t, e) -> {
            this.interrupt(Adapter.Signal.SIGFPE, e); // sigfpe
            if (this.uncaughtExceptionHandler != null) {
                this.uncaughtExceptionHandler.fn(t, e);
            }
        });

        this.driverInstance.setThread(thread);

        this.state = State.RUNNING;
        this.thread.start();
    }

    @Override
    public void destroy(ChannelSession channel) {
        this.interrupt(Adapter.Signal.SIGHUP, "process destroyed by outside reason."); // sighup
    }

    public AdapterImpl(
            Number processId,
            Class<Driver_t> driver,
            Arg_t argv) {

        this.driver = driver;
        this.processId = processId;
        this.argv = argv;
        this.logger = org.slf4j.LoggerFactory.getLogger(AdapterImpl.class);
        this.state = State.READY; // 1 constructed
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.exitCallback = callback;
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
