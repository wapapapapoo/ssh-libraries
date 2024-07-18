package one.iolab.app.iostreamwrapper;

import java.io.IOException;
import java.io.OutputStream;

public class ByteConsumerImpl implements ByteConsumer {

    private OutputStream out;

    public OutputStream getStream() {
        return this.out;
    }

    public ByteConsumerImpl(OutputStream out) {
        this.out = out;
    }

    public void send(byte[] bytes, int off, int size) throws IOException {
        this.out.write(bytes, off, size);
    }

    public void flush() throws IOException {
        this.out.flush();
    }

}
