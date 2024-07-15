package one.iolab.app.iostreamwrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.sshd.common.channel.exception.SshChannelClosedException;

public class ByteProviderImpl implements ByteProvider {
    private int bufferSize = 1024;

    private InputStream in;
    private int currentFrameSize = 0;
    private byte[] buffer = new byte[this.bufferSize];
    private int bufferPtr = 0;
    private boolean closed = false;

    public ByteProviderImpl(InputStream in) {
        this.in = in;
    }

    private void receive() throws IOException {
        if (this.closed) {
            return;
        }

        int ret = 0;

        try {
            ret = this.in.read(this.buffer, 0, this.bufferSize);
        } catch (SshChannelClosedException e) {
            ret = -1;
        }

        if (ret == -1 || ret == 0) {
            this.closed = true;
            ret = 0;
        }

        this.currentFrameSize = ret;
        this.bufferPtr = 0;
    }

    public void flush() {
        this.bufferPtr = this.currentFrameSize;
    }

    public byte[] getByte() throws IOException {
        return this.getByte(true);
    }

    public byte[] getByte(boolean block) throws IOException {
        if (this.closed) {
            return null;
        }

        if (this.bufferPtr < this.currentFrameSize) {
            return new byte[] { this.buffer[this.bufferPtr++] };
        } else if (block) {
            this.receive();
            return this.getByte(false);
        } else {
            return null;
        }
    }

    public byte[] get() throws IOException {
        if (this.closed) {
            return null;
        }

        if (this.bufferPtr < this.currentFrameSize) {
            byte[] ret = Arrays.copyOfRange(this.buffer, this.bufferPtr, this.currentFrameSize);
            this.bufferPtr = this.currentFrameSize;
            return ret;
        } else {
            this.receive();
            return this.get();
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    public boolean available() {
        return this.bufferPtr < this.currentFrameSize;
    }
}
