package one.iolab.app.drivers;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.channel.ChannelSession;
import org.slf4j.Logger;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import one.iolab.app.adapters.Adapter;
import one.iolab.app.adapters.AdapterImpl;
import one.iolab.app.iostreamwrapper.ByteConsumer;
import one.iolab.app.iostreamwrapper.ByteConsumerNullImpl;
import one.iolab.app.iostreamwrapper.ByteProvider;
import one.iolab.app.iostreamwrapper.ByteProviderNullImpl;

@Data
@Getter
@Setter
public abstract class BaseDriver<Arg_t> implements Runnable {
    public static final String driverName = "AnonymousDriver";

    protected ByteProvider cin = new ByteProviderNullImpl();
    protected ByteConsumer cout = new ByteConsumerNullImpl();
    protected ByteConsumer cerr = new ByteConsumerNullImpl();

    protected Environment env;
    protected ChannelSession channel = null;
    protected Thread thread = null;

    protected Arg_t argv = null;
    protected Number processId = -1;

    protected Adapter adapter = null;
    protected AdapterImpl<? extends BaseDriver<Arg_t>, Arg_t>.Callback callback;

    protected Logger logger = null;

    public abstract void run();

}
