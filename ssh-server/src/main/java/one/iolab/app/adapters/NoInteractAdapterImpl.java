package one.iolab.app.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// import org.slf4j.Logger;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;

public class NoInteractAdapterImpl implements Adapter {
    private byte[][] message;

    private OutputStream out;
    private ExitCallback callback;

    // private Logger logger;
    private State state = State.NULL;

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
        this.state = State.RUNNING;

        try {
            for (byte[] msg : this.message) {
                this.out.write(msg, 0, msg.length);
            }
            this.out.flush();
        } catch (IOException e) {

        }

        this.state = State.INTED;
        this.callback.onExit(1, "exited immediately");
    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        channel.close();
        this.state = State.DESTROY;
    }

    public void shutdown() {
    }

    public NoInteractAdapterImpl(byte[]... message) {
        this.message = message;
        // this.logger =
        // org.slf4j.LoggerFactory.getLogger(NoInteractAdapterImpl.class);
        this.state = State.NULL;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void setInputStream(InputStream in) {
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
        this.state = State.READY;
    }

    @Override
    public void setErrorStream(OutputStream err) {
    }
}
