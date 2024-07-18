package one.iolab.app.adapters;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;

public interface Adapter extends Command {

    public static class Signal {
        public static final int SIGHUP = 1;
        public static final int SIGINT = 2;
        public static final int SIGQUIT = 3;
        public static final int SIGILL = 4;
        public static final int SIGFPE = 8;
        public static final int SIGKILL = 9;
        public static final int SIGALRM = 14;
        public static final int SIGTERM = 15;
    }

    void interrupt(int signal, Object arg);

    public enum State {
        NULL(0),
        READY(1),
        RUNNING(2),
        BLOCKED(3),
        INTED(4),
        STOPED(5);

        private final int value;

        private State(int value) {
            this.value = value;
        }

        public String toString() {
            switch (this.value) {
                case 0:
                    return "NULL";
                case 1:
                    return "READY";
                case 2:
                    return "RUNNING";
                case 3:
                    return "BLOCKED";
                case 4:
                    return "INTED";
                case 5:
                    return "STOPED";
                default:
                    assert false;
                    return null;
            }
        }
    }

    Adapter.State getState();

    void setState(Adapter.State state);

    ServerSession getSession();

    ChannelSession getChannel();
}
