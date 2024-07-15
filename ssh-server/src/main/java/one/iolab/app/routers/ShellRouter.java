/**
 * @note 如果这个文件报了一堆错，不要在意，因为傻逼vscode不认识kotlin写的类
 */
package one.iolab.app.routers;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import one.iolab.app.drivers.BaseShellDriver;
import one.iolab.app.pools.PoolHolder;
import one.iolab.app.pools.SCPSessionPool;
import one.iolab.app.pools.ShellSessionPool;

import org.apache.sshd.server.command.Command;

import one.iolab.app.adapters.NoInteractAdapterImpl;
import one.iolab.app.adapters.AdapterImpl;
import one.iolab.app.drivers.BaseDriver;

/**
 * @class SSHCommonShellRouter
 * 
 * @brief 从SSHShellRouter二级路由一般用户的终端连接，兼做连接池，限制并发
 */
public class ShellRouter {

    public PoolHolder<ShellSessionPool, AdapterImpl<BaseShellDriver>> poolHolder = new PoolHolder<>(
            ShellSessionPool.class);

    private Map<String, Object> parseCommand(String command) {
        Map<String, Object> argv = new HashMap<>();
        argv.put("command", command);
        return argv;
    }

    private Command createShellAdapter(int processId, String command) {
        AdapterImpl<BaseShellDriver> newAdapter = new AdapterImpl<>(
                processId,
                BaseShellDriver.class,
                parseCommand(command));

        this.poolHolder.put(processId, newAdapter);

        return newAdapter;
    }

    private Command createRejectedAdapter(String reason) {
        return new NoInteractAdapterImpl(("\r\n\033[37m\033[44m" + reason + "\033[0m\r\n").getBytes());
    }

    public synchronized Command createAdapter(String command) {

        int t = 2;

        do {
            int processId = this.poolHolder.firstFreeSlot();

            if (processId != -1) { // 连接池还有余量
                return createShellAdapter(processId, command);
            }

            this.poolHolder.flush(); // 手动gc
        } while (t-- > 0);

        return createRejectedAdapter(" 连接池已满，请稍后使用 ");
    }
}
