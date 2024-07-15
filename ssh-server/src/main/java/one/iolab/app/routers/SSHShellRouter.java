package one.iolab.app.routers;

import org.apache.sshd.server.shell.ShellFactory;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import one.iolab.app.config.Config;
// import one.iolab.app.config.Symbol;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
// import org.apache.sshd.server.session.ServerSession;

/**
 * @class SSHShellRouter
 * 
 * @brief 路由不带命令的终端连接
 */
public class SSHShellRouter implements ShellFactory {
    public ShellRouter shellAdapterFactory;
    // private Logger logger = LoggerFactory.getLogger(SSHShellRouter.class);

    public SSHShellRouter() {
        this.shellAdapterFactory = new ShellRouter();
    }

    public Command createShell(ChannelSession channel) {
        // ServerSession session = channel.getServerSession();

        // Symbol.USER_ROLE role = session.getAttribute(Symbol.SESSION_USER_ROLE);
        // 做特殊的逻辑判断
        // logger.info(role.toString());

        // 一般用户适用的连接，转发到ShellRouter做下一级路由
        return shellAdapterFactory.createAdapter("");
    }
}
