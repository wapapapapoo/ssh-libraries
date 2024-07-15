package one.iolab.app.iostreamwrapper;

public class ByteProviderNullImpl implements ByteProvider {
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
