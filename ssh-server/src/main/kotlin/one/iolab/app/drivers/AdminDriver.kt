package one.iolab.app.drivers

import java.lang.reflect.Field
import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Stack
import kotlinx.coroutines.*
import one.iolab.app.adapters.Adapter
import one.iolab.app.config.Config
import one.iolab.app.pools.ProcessPool
import one.iolab.utils.CliUtils
import one.iolab.utils.CommandUtils
import org.apache.sshd.common.util.net.SshdSocketAddress
import org.apache.sshd.server.session.ServerSession

public open class AdminDriver() : BaseDriver<Any?>() {

    companion object {
        @JvmField public val driverName: String = "AdminShell"
    }

    private val charset: Charset = StandardCharsets.UTF_8

    private val history: Stack<ArrayList<String>> = Stack()

    private fun getLn(): String {
        val next: Stack<ArrayList<String>> = Stack()
        var buffer: ArrayList<String> = ArrayList()

        this.cout.accept("\r\n".toByteArray())
        while (!this.thread.isInterrupted()) {
            var command: String =
                    CliUtils.utf8InputBuffer(
                            this.cin,
                            this.cout,
                            "$ ",
                            2,
                            buffer,
                            { this.thread.isInterrupted() }
                    )

            if (command.equals("prev")) {
                if (!history.isEmpty()) {
                    next.push(buffer)
                    buffer = history.pop()
                }
            } else if (command.equals("next")) {
                if (!next.isEmpty()) {
                    history.push(buffer)
                    buffer = next.pop()
                }
            } else {
                history.push(buffer)
                this.cout.accept("\r\n\r\n".toByteArray())
                return command
            }
        }

        return "exit"
    }

    private fun putLn(str: String) {
        try {
            this.cout.accept("$str\r\n".toByteArray(this.charset))
        } catch (e: Exception) {
            this.callback.raise(0, e)
        }
    }

    private fun listProcess() {
        putLn("Server processes:")
        putLn("pid   | driver name          | state    | idle start          | owner")

        val dtf: DateTimeFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss").withZone(ZoneOffset.ofHours(0))

        ProcessPool()
                .getPoolHolder()
                .forEach({ record: ProcessPool.Record ->
                    val driverNameField: Field = record.driver.getField("driverName")
                    val session: ServerSession = record.adapter.getSession()
                    val addr: InetSocketAddress =
                            SshdSocketAddress.toInetSocketAddress(session.getRemoteAddress())

                    putLn(
                            String.format(
                                    "%-5d | %-20s | %-8s | %-19s | %s:%d",
                                    record.processId,
                                    driverNameField.get(null),
                                    record.adapter.getState().toString(),
                                    dtf.format(session.getIdleTimeoutStart()),
                                    addr.getHostName(),
                                    addr.getPort()
                            )
                    )
                })
    }

    private fun killProcess(processId: Int) {
        ProcessPool()
                .getPoolHolder()
                .get(processId)
                .adapter
                .interrupt(Adapter.Signal.SIGKILL, "process killed")
    }

    private fun flushProcess() {
        ProcessPool()
                .getPoolHolder()
                .flush({ record -> record.adapter.getState() == Adapter.State.STOPED })
    }

    private fun touchProcess(processId: Int) {
        ProcessPool().getPoolHolder().get(processId).adapter.getSession().resetIdleTimeout()
    }

    private fun detachProcess(processId: Int) {
        ProcessPool().getPoolHolder().put(processId, null)
    }

    private fun expandPool(size: Int) {
        ProcessPool().getPoolHolder().expand(size)
    }

    private fun lockDown() {
        Config.isRunning.set(false)
    }

    private fun openUp() {
        Config.isRunning.set(true)
    }

    private fun openBash(command: String) =
            runBlocking {
                // val processBuilder: ProcessBuilder = ProcessBuilder(*CommandUtils.parse(command))
                // val process = processBuilder.start()

                // // 将进程的输出重定向到指定的OutputStream
                // val processOutputStream = process.getOutputStream()

                // launch {
                //     val buffer = ByteArray(1024)
                //     var read: Int = cin.getStream().read(buffer)
                //     while (read != -1 && read != 0) {
                //         processOutputStream.write(buffer, 0, read)
                //         read = cin.getStream().read(buffer)
                //     }
                // }

                // // 将进程的输入重定向到指定的InputStream
                // val processInputStream = process.getInputStream()

                // launch {
                //     val buffer = ByteArray(1024)
                //     var read: Int
                //     while (processInputStream.read(buffer).also { read = it } != -1) {
                //         cout.getStream().write(buffer, 0, read)
                //     }
                // }

                // // 等待进程结束
                // process.waitFor()
            }

    private fun welcome() {
        this.cout.accept("Admin Shell\r\nEnter `help` for further details.\r\n".toByteArray())
    }

    public override fun run() {

        while (!this.thread.isInterrupted()) {
            var command: String = getLn()
            var args: Array<String> = CommandUtils.parse(command)

            if (args.size == 0) {
                continue
            }

            try {
                when (args[0]) {
                    "exit" -> {
                        this.callback.onExit(0)
                        return
                    }
                    "clear" -> {
                        // 我他妈是失了智才会用kotlin啊
                        var sb = StringBuilder()
                        sb.append(27.toChar())
                        sb.append("[2J")
                        sb.append(27.toChar())
                        sb.append("[H")
                        this.cout.accept(sb.toString().toByteArray())
                    }
                    "lc", "list-process" -> {
                        this.listProcess()
                    }
                    "kick", "kill-process" -> {
                        if (args.size < 2) continue
                        this.killProcess(args[1].toInt())
                    }
                    "detach", "detach-process" -> {
                        if (args.size < 2) continue
                        this.detachProcess(args[1].toInt())
                    }
                    "expand-pool" -> {
                        if (args.size < 2) continue
                        this.expandPool(args[1].toInt())
                    }
                    "touch", "touch-process" -> {
                        if (args.size < 2) continue
                        this.touchProcess(args[1].toInt())
                    }
                    "flush", "flush-process" -> {
                        this.flushProcess()
                    }
                    "lock" -> {
                        this.lockDown()
                    }
                    "open" -> {
                        this.openUp()
                    }
                    "bash" -> {
                        this.openBash("bash")
                    }
                    else -> {
                        this.cout.accept("Unknown command: $command\r\n".toByteArray(this.charset))
                    }
                }
            } catch (e: Exception) {
                this.callback.raise(0, e)
            }
        }
    }
}
