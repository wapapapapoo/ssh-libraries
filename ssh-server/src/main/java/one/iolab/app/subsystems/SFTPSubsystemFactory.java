package one.iolab.app.subsystems;

import java.io.IOException;

import org.apache.sshd.common.SshConstants;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.subsystem.SubsystemFactory;

public class SFTPSubsystemFactory implements SubsystemFactory {

    @Override
    public String getName() {
        return "sftp";
    }

    @Override
    public Command createSubsystem(ChannelSession channel) {
        try {
            channel.getSession().disconnect(
                    SshConstants.SSH2_DISCONNECT_HOST_NOT_ALLOWED_TO_CONNECT, "\r\nUnsupport Subsystem sftp");
            channel.close();
        } catch (IOException e) {
        } finally {
            channel.getSession().close(true);
        }
        return null;
    }

}
