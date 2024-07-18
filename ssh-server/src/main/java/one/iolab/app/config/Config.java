package one.iolab.app.config;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
public class Config {

    public static Config init = new Config();

    private Duration idleTimeout = Duration.ofMinutes(3);
    private Duration authTimeout = Duration.ofSeconds(30);

    private String kbServerInteractiveName = "";
    private String kbServerInteractiveInstruction = "";
    private String kbServerInteractiveLang = "zh-CN";
    private String kbServerInteractivePrompt = "\r\n欢迎使用io文库\r\n按回车键继续...";

    private String defaultIp = "0.0.0.0";
    private short defaultPort = 22;

    private int initPoolSize = 3;

    private String localURI = "ssh.iolab.one";
    private int localPort = 22;

    private String adminUserName = "admin";
    private String adminPassword = "pass";

    public static AtomicBoolean isRunning = new AtomicBoolean(true);

}
