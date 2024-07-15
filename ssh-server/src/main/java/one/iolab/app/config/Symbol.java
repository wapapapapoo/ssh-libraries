package one.iolab.app.config;

import org.apache.sshd.common.AttributeRepository.AttributeKey;

public final class Symbol {

    public enum PROCESS_STRATEGY {
        TEXT_SHELL(0),
        TUI_SHELL(1),
        SCP(2),
        SCP_PLUS(3),
        CLIENT(4),
        ADMIN(5),
        ;

        int strategy;

        private PROCESS_STRATEGY(int strategy) {
            this.strategy = strategy;
        }

        public String toString() {
            String desc = "PROCESS_STRATEGY_";

            switch (this.strategy) {
                case 0:
                    desc += "TEXT_SHELL";
                    break;
                case 1:
                    desc += "TUI_SHELL";
                    break;
                case 2:
                    desc += "SCP";
                    break;
                case 3:
                    desc += "SCP_PLUS";
                    break;
                case 4:
                    desc += "CLIENT";
                    break;
                case 5:
                    desc += "ADMIN";
                    break;
                default:
                    desc += "UNKNOWN";
                    break;
            }

            return desc;
        }
    }

    public static final AttributeKey<PROCESS_STRATEGY> SESSION_PROCESS_STRATEGY = new AttributeKey<PROCESS_STRATEGY>();

}
