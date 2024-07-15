package one.iolab;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import one.iolab.app.config.Config;
import one.iolab.app.routers.CommandRouter;
import one.iolab.app.routers.SSHShellRouter;
import one.iolab.app.sshdconfig.MyKeyPairProvider;
import one.iolab.app.sshdconfig.MyKeyboardInteractiveAuthenticator;
import one.iolab.app.sshdconfig.MyPasswordAuthenticator;
import one.iolab.app.sshdconfig.MyPreAuthenticator;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class }) // 暂时没有数据源
public class App implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        String bind_ip = Config.init.getDefaultIp();
        int bind_port = Config.init.getDefaultPort();

        for (String arg : args) {
            if (arg.startsWith("-l")) {
                bind_ip = arg.substring(2);
            }

            if (arg.startsWith("-p")) {
                bind_port = Integer.parseInt(arg.substring(2));
            }
        }

        Config.init.setLocalPort(bind_port);

        SshServer sshd = SshServer.setUpDefaultServer();

        sshd.setPort(bind_port);
        sshd.setHost(bind_ip);

        sshd.setKeyPairProvider(new MyKeyPairProvider());

        sshd.setKeyboardInteractiveAuthenticator(new MyKeyboardInteractiveAuthenticator());

        // ssh server
        SSHShellRouter shellRouter = new SSHShellRouter();
        sshd.setShellFactory(shellRouter);
        // sshd.setShellFactory(new ProcessShellFactory("cmd", "-i"));

        // command
        CommandRouter commandRouter = new CommandRouter();
        sshd.setCommandFactory(commandRouter);

        sshd.start();
    }
}
