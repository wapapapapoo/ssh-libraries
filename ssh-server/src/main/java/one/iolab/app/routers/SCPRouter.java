/**
 * @note 如果这个文件报了一堆错，不要在意，因为傻逼vscode不认识kotlin写的类
 */
package one.iolab.app.routers;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import one.iolab.app.drivers.SCPDriver;
import one.iolab.app.pools.PoolHolder;
import one.iolab.app.pools.SCPSessionPool;

import org.apache.sshd.server.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.iolab.app.adapters.NoInteractAdapterImpl;
import one.iolab.app.adapters.AdapterImpl;
import one.iolab.utils.CommandUtils;

/**
 * @class SCPRouter
 * 
 * @brief SCP连接的连接池
 */
public class SCPRouter {

    public PoolHolder<SCPSessionPool, AdapterImpl<SCPDriver>> poolHolder = new PoolHolder<>(SCPSessionPool.class);

    private Map<String, Object> parseCommand(String command) {
        Map<String, Object> argv = new HashMap<>();
        argv.put("command", CommandUtils.parseSCP(command));
        return argv;
    }

    private Command createScpAdapter(int processId, String command) {

        AdapterImpl<SCPDriver> newAdapter = new AdapterImpl<>(
                processId,
                SCPDriver.class,
                this.parseCommand(command));

        this.poolHolder.put(processId, newAdapter);

        return newAdapter;
    }

    public synchronized Command createAdapter(String command) {

        int t = 2;

        do {
            int processId = this.poolHolder.firstFreeSlot();

            if (processId != -1) { // 连接池还有余量
                return createScpAdapter(processId, command);
            }

            this.poolHolder.flush(); // 手动gc
        } while (t-- > 0);

        return null;
    }
}
