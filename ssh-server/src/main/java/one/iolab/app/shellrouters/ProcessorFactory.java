package one.iolab.app.shellrouters;

import org.apache.sshd.server.shell.ShellFactory;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import one.iolab.app.config.Config;
// import one.iolab.app.config.Symbol;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
// import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.command.CommandFactory;

/**
 * @class SSHShellRouter
 * 
 * @brief 路由不带命令的终端连接
 */
public class ProcessorFactory implements ShellFactory, CommandFactory {
    public ProcessorRouter processFactory;

    // private Logger logger = LoggerFactory.getLogger(MyShellFactory.class);

    public ProcessorFactory() {
        this.processFactory = new ProcessorRouter();
    }

    public Command createCommand(ChannelSession channel, String command) {
        return processFactory.createProcess(command, channel);
    }

    public Command createShell(ChannelSession channel) {
        return processFactory.createProcess("", channel);
    }
}
