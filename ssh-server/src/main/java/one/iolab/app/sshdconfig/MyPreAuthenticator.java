package one.iolab.app.sshdconfig;

import java.net.InetSocketAddress;

import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.auth.preauth.PreAuthenticator;
import org.apache.sshd.server.session.ServerSession;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import one.iolab.app.config.AddrFilter;
import one.iolab.app.config.Config;
import one.iolab.app.config.Symbol;
import one.iolab.app.config.Symbol.PROCESS_STRATEGY;

public class MyPreAuthenticator implements PreAuthenticator {

    // private Logger logger = LoggerFactory.getLogger(MyPreAuthenticator.class);

    public static String welcomeBanner() {
        return """

                _________________________________________________________
                 __    ______        ______  __       __    __  .______
                |  |  /  __  \\      /      ||  |     |  |  |  | |   _  \\
                |  | |  |  |  |    |  ,----'|  |     |  |  |  | |  |_)  |
                |  | |  |  |  |    |  |     |  |     |  |  |  | |   _  <
                |  | |  `--'  |    |  `----.|  `----.|  `--'  | |  |_)  |
                |__|  \\______/      \\______||_______| \\______/  |______/

                 _  _ |_    _      |_  | .  _  | . |_   _  _   _ .  _   _
                _> _> | |  |_) |_| |_) | | (_  | | |_) |  (_| |  | (/_ _>
                           |
                ---------------------------------------------------------
                ssh-libraries v0.1.0, Copyright (C) 2024 IO Club
                @licence                                          GPL-3.0
                @author                  wapapapapoo<wapapapapoo@163.com>
                """;
    }

    public static String tuiWelcomeBanner() {
        return String.format(welcomeBanner() + """

                欢迎访问 IO 社公共文库。
                请确保您的终端尺寸不小于 120x30，接收 UTF-8 编码的文本，
                并支持 ANSI 控制序列。若您的终端不符合条件，请使用
                nofeedback 版：
                    ssh nofeedback@%s -p %d
                您可以使用 help 获取帮助，键入`^C`退出。
                ---------------------------------------------------------
                Press any key to continue...                 按任意键继续
                """,
                Config.init.getLocalURI(),
                Config.init.getLocalPort());
    }

    public static String nofeedbackWelcomeBanner() {
        return String.format(welcomeBanner() + """

                欢迎访问 IO 社公共文库。
                使用 help 获取帮助，键入`^C`退出。

                Enter `help` for more infomation, Enter `^C` to exit.
                ---------------------------------------------------------
                Press any key to continue...                 按任意键继续
                """,
                Config.init.getLocalURI(),
                Config.init.getLocalPort());
    }

    public static String blockedBanner(String host, int port) {
        return String.format(welcomeBanner() + """

                感谢您访问 IO 社公共文库。
                您的地址 %s:%d 已被屏蔽，请联系管理员解除。
                ---------------------------------------------------------
                关于爬虫的说明：

                IO 社公共文库允许爬虫以及脚本的访问，但短时间内大量下载会
                导致您的 ip 被屏蔽。如果您需要大量下载，或架设镜像，请直
                接联系运营人员。
                ---------------------------------------------------------
                Press any key to exit...                     按任意键退出
                """, host, port);
    }

    public static String notfoundBanner() {
        return String.format(welcomeBanner() + """

                欢迎访问 IO 社公共文库。
                访问方式：
                终端:
                    ssh shell@%s -p %d
                没有额外反馈的终端:
                    ssh nofeedback@%s -p %d
                scp:
                    scp -P %d scp@%s:<remote file> <local file>
                    scp -r -P %d scp@%s:<remote dir> <local dir>
                    # 目前只支持获取文件。
                    # windows 端的 scp 依赖 sftp，故不支持。需要获取文件
                    # 时请使用客户端。
                客户端:
                    前往 http://%s:80 获取客户端。
                ---------------------------------------------------------
                Press any key to exit...                     按任意键退出
                """,
                Config.init.getLocalURI(), Config.init.getLocalPort(),
                Config.init.getLocalURI(), Config.init.getLocalPort(),
                Config.init.getLocalPort(), Config.init.getLocalURI(),
                Config.init.getLocalPort(), Config.init.getLocalURI(),
                Config.init.getLocalURI());
    }

    public static String scpBanner() {
        return String.format(welcomeBanner() + """

                ---------------------------------------------------------
                Press any key to continue...                 按任意键继续
                """);
    }

    public boolean authenticate(String username, ServerSession session, AddPromptLambda addPrompt) {

        InetSocketAddress remoteAddress = SshdSocketAddress.toInetSocketAddress(session.getRemoteAddress());
        if (!AddrFilter.isAllowed(remoteAddress)) {
            addPrompt.add(tuiWelcomeBanner());
            return false;
        }

        switch (username) {
            case "shell":
                session.setAttribute(Symbol.SESSION_PROCESS_STRATEGY, PROCESS_STRATEGY.TUI_SHELL);
                addPrompt.add(tuiWelcomeBanner());
                addPrompt.add("\r\n"
                        + "-".repeat(57)
                        + "\r\nTerm of Service\r\n\r\nnothing...\r\n"
                        + "-".repeat(57)
                        + "\r\nConfirm:\r\n");
                return true;

            case "nofeedback":
                session.setAttribute(Symbol.SESSION_PROCESS_STRATEGY, PROCESS_STRATEGY.TEXT_SHELL);
                addPrompt.add(nofeedbackWelcomeBanner());
                return true;

            case "scp":
                session.setAttribute(Symbol.SESSION_PROCESS_STRATEGY, PROCESS_STRATEGY.SCP);
                addPrompt.add(scpBanner());
                return true;

            case "scp+":
                session.setAttribute(Symbol.SESSION_PROCESS_STRATEGY, PROCESS_STRATEGY.SCP_PLUS);
                addPrompt.add("");
                return true;

            case "client":
                session.setAttribute(Symbol.SESSION_PROCESS_STRATEGY, PROCESS_STRATEGY.CLIENT);
                addPrompt.add("");
                return true;

            default:
                if (username.equals(Config.init.getAdminUserName())) {
                    session.setAttribute(Symbol.SESSION_PROCESS_STRATEGY, PROCESS_STRATEGY.ADMIN);
                    addPrompt.add(
                            welcomeBanner()
                                    + "\r\n正在进入后台。\r\n"
                                    + "-".repeat(57)
                                    + "\r\n请输入后台密码:\r\n");
                    return true;
                }

                addPrompt.add(notfoundBanner());
                return false;
        }
    }
}
