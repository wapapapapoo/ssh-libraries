package one.iolab.app.routers;

import java.io.IOException;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.iolab.utils.CommandUtils;

/**
 * @class CommandRouter
 * 
 * @brief 路由带命令的连接，如scp等
 */
public class CommandRouter implements CommandFactory {
    public SCPRouter scpAdapterFactory = new SCPRouter();

    private Logger logger = LoggerFactory.getLogger(CommandRouter.class);

    public Command createCommand(ChannelSession channel, String command) throws IOException {
        String[] parsedCommand = CommandUtils.parse(command);

        logger.info(command);

        switch (parsedCommand[0]) {
            case "scp":
                return scpAdapterFactory.createAdapter(command);
            default:
                return null;
        }
    }
}
