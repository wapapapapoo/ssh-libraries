package one.iolab.app.adapters;

import org.apache.sshd.server.command.Command;

public interface Adapter extends Command {

    /**
     * Shutdown the adapter
     */
    void shutdown();

    public enum State {
        NULL(0),
        READY(1),
        RUNNING(2),
        BLOCKED(3),
        INTED(4),
        DESTROY(5);

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
                    return "DESTROY";
                default:
                    assert false;
                    return null;
            }
        }
    }

    Adapter.State getState();

    void setState(Adapter.State state);
}
