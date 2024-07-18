package one.iolab.app.iostreamwrapper;

import java.io.OutputStream;

public class ByteConsumerNullImpl implements ByteConsumer {

    public OutputStream getStream() {
        return null;
    }

    public void send(byte[] bytes, int off, int size) {
        // do nothing...
    }

    public void flush() {
        // do nothing...
    }
}
