package one.iolab.app.iostreamwrapper;

import java.io.InputStream;

public class ByteProviderNullImpl implements ByteProvider {

    public InputStream getStream() {
        return null;
    }

    public byte[] getByte() {
        return null;
    }

    public byte[] get() {
        return null;
    }

    public void flush() {
        // do nothing...
    }

    public boolean isClosed() {
        return true;
    }

    public boolean available() {
        return false;
    }
}
