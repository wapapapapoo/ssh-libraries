package one.iolab.app.sshdconfig;

import java.net.InetSocketAddress;

import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.iolab.app.config.Config;
import one.iolab.app.config.Symbol;
import one.iolab.app.config.Symbol.PROCESS_STRATEGY;

public class MyPasswordAuthenticator implements PasswordAuthenticator {

    private Logger logger = LoggerFactory.getLogger(MyPasswordAuthenticator.class);

    @Override
    public boolean authenticate(String username, String password, ServerSession session)
            throws PasswordChangeRequiredException, AsyncAuthException {

        InetSocketAddress remoteAddress = SshdSocketAddress.toInetSocketAddress(session.getRemoteAddress());

        PROCESS_STRATEGY strategy = session.getAttribute(Symbol.SESSION_PROCESS_STRATEGY);

        if (strategy == null) {
            return false;
        }

        logger.info("PasswordAuthenticator: Received Connection (Strategy {}): {}:{}@{}:{}",
                session.getAttribute(Symbol.SESSION_PROCESS_STRATEGY).toString(),
                username,
                password,
                remoteAddress.getHostName(),
                remoteAddress.getPort());

        switch (strategy) {

            case TEXT_SHELL:
            case TUI_SHELL:
            case SCP:
            case SCP_PLUS:
            case CLIENT:
                return true;

            case ADMIN:
                assert username.equals(Config.init.getAdminUserName());

                if (password.equals(Config.init.getAdminPassword())) {
                    logger.warn("Admin Login: {}:{}",
                            remoteAddress.getHostName(),
                            remoteAddress.getPort());

                    return true;
                } else {
                    logger.warn("Failed Admin Login: {}:{}@{}:{}",
                            username,
                            password,
                            remoteAddress.getHostName(),
                            remoteAddress.getPort());

                    return false;
                }

            default:
                return false;
        }
    }

}
