package one.iolab.app.iostreamwrapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * @interface ByteProvider
 * 
 * @brief byte provider between drivers and adapters
 */
public interface ByteProvider {

    InputStream getStream();

    /**
     * @brief get a byte from input stream
     * @return byte[1] or null when closed
     * @throws IOException
     */
    byte[] getByte() throws IOException;

    /**
     * @brief get all bytes in user buffer
     * @return byte[] or null when closed
     * @throws IOException
     */
    byte[] get() throws IOException;

    /**
     * @brief clear user buffer
     */
    void flush();

    /**
     * @brief check if input stream is closed
     * @return
     */
    boolean isClosed();

    /**
     * @brief check if user buffer not empty
     * @return
     */
    boolean available();
}
