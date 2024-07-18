package one.iolab.app.iostreamwrapper;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @interface ByteConsumer
 * 
 * @brief byte consumer between drivers and adapters
 */
public interface ByteConsumer {

    OutputStream getStream();

    void send(byte[] bytes, int off, int size) throws IOException;

    default void send(byte[] bytes) throws IOException {
        this.send(bytes, 0, bytes.length);
    };

    void flush() throws IOException;

    default void accept(byte[] bytes) throws IOException {
        this.send(bytes);
        this.flush();
    }

}
