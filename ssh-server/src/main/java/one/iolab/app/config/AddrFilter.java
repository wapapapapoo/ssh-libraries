package one.iolab.app.config;

import java.net.InetSocketAddress;

public class AddrFilter {
    public static boolean isAllowed(InetSocketAddress addr) {
        return true;
    }
}
