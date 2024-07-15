package one.iolab.app.drivers;

import java.util.HashMap;
import java.util.Map;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import one.iolab.app.adapters.Adapter;
import one.iolab.app.iostreamwrapper.ByteConsumer;
import one.iolab.app.iostreamwrapper.ByteConsumerNullImpl;
import one.iolab.app.iostreamwrapper.ByteProvider;
import one.iolab.app.iostreamwrapper.ByteProviderNullImpl;

@Data
@Getter
@Setter
public abstract class BaseDriver implements Runnable {

    protected ByteProvider cin = new ByteProviderNullImpl();
    protected ByteConsumer cout = new ByteConsumerNullImpl();
    protected ByteConsumer cerr = new ByteConsumerNullImpl();

    protected Environment env;
    protected ChannelSession channel = null;
    protected ExitCallback exitCallback;
    protected Thread thread = null;

    protected Map<String, Object> argv = new HashMap<String, Object>();

    protected String driverName = "AnonymousDriver";
    protected Number processId = -1;

    protected Adapter adapter = null;

    public abstract void run();

    // callback
    public void shutdown() {
        // do nothing...
    }
}
