package one.iolab.app.shellrouters;

import one.iolab.app.pools.PoolHolder;
import one.iolab.app.pools.ProcessPool;
import one.iolab.utils.CommandUtils;

import java.nio.charset.StandardCharsets;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
// import org.slf4j.LoggerFactory;

import one.iolab.app.adapters.NoInteractAdapterImpl;
import one.iolab.app.config.Symbol;
import one.iolab.app.adapters.Adapter;
import one.iolab.app.adapters.AdapterImpl;
import one.iolab.app.drivers.BaseDriver;

// drivers in kotlin
// 如果这里报了一堆错，不要管它
import one.iolab.app.drivers.TUIShellDriver;
import one.iolab.app.drivers.TextShellDriver;
import one.iolab.app.drivers.SCPDriver;
import one.iolab.app.drivers.SCPPlusDriver;
import one.iolab.app.drivers.ClientDriver;
import one.iolab.app.drivers.AdminDriver;

/**
 * @class ProcessorRouter
 * 
 * @brief 路由到driver，检查入参
 */
public class ProcessorRouter {
    private <Driver_t extends BaseDriver<Arg_t>, Arg_t> Adapter newAdapter(
            int processId,
            Class<Driver_t> driver,
            Arg_t command) {

        return new AdapterImpl<Driver_t, Arg_t>(processId, driver, command);
    }

    private Adapter generateProcess(int processId, String command, ChannelSession channel) {
        Adapter adapter = null;
        Class<? extends BaseDriver<?>> driver = null;

        ServerSession session = channel.getSession();
        Symbol.PROCESS_STRATEGY strategy = session.getAttribute(Symbol.SESSION_PROCESS_STRATEGY);

        if (strategy == null) {
            throw new IllegalStateException(
                    "Failed to generate processor: no process strategy for the current session");
        }

        switch (strategy) {
            case TEXT_SHELL -> {
                Class<TextShellDriver> driverClass = TextShellDriver.class;
                driver = driverClass;
                adapter = newAdapter(
                        processId,
                        driverClass,
                        null);
            }
            case TUI_SHELL -> {
                Class<TUIShellDriver> driverClass = TUIShellDriver.class;
                driver = driverClass;
                adapter = newAdapter(
                        processId,
                        driverClass,
                        null);
            }
            case SCP -> {
                Class<SCPDriver> driverClass = SCPDriver.class;
                driver = driverClass;
                adapter = newAdapter(
                        processId,
                        driverClass,
                        CommandUtils.parseSCP(command));
            }
            case SCP_PLUS -> {
                Class<SCPPlusDriver> driverClass = SCPPlusDriver.class;
                driver = driverClass;
                adapter = newAdapter(
                        processId,
                        driverClass,
                        CommandUtils.parse(command));
            }
            case SFTP -> {
                throw new RuntimeException("SFTP Driver not supported");
            }
            case CLIENT -> {
                Class<ClientDriver> driverClass = ClientDriver.class;
                driver = driverClass;
                adapter = newAdapter(
                        processId,
                        driverClass,
                        null);
            }
            case ADMIN -> {
                Class<AdminDriver> driverClass = AdminDriver.class;
                driver = driverClass;
                adapter = newAdapter(
                        processId,
                        driverClass,
                        null);
                return adapter;
            }
        }

        ProcessPool.Record record = new ProcessPool.Record(
                processId,
                channel,
                adapter,
                driver);

        new ProcessPool().getPoolHolder().put(processId, record);
        return adapter;
    }

    private Adapter generateFailedProcess(String message) {
        return new NoInteractAdapterImpl(message.getBytes(StandardCharsets.UTF_8));
    }

    public synchronized Adapter createProcess(String command, ChannelSession channel) {
        Symbol.PROCESS_STRATEGY strategy = channel.getSession().getAttribute(Symbol.SESSION_PROCESS_STRATEGY);
        if (strategy != null && strategy == Symbol.PROCESS_STRATEGY.ADMIN) {
            return generateProcess(-1, command, channel);
        }

        int t = 2;
        do {
            int processId = new ProcessPool().getPoolHolder().firstFreeSlot();
            if (processId != -1) { // 连接池还有余量
                return generateProcess(processId, command, channel);
            }
            new ProcessPool().getPoolHolder().flush(process -> {
                return process.adapter.getState() == Adapter.State.STOPED;
            });
        } while (t-- > 0);

        return generateFailedProcess("\r\n\033[37m\033[44m 连接池已满，请稍后使用 \033[0m\r\n");
    }
}
