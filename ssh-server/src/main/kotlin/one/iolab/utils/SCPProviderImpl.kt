package one.iolab.utils

import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import one.iolab.app.iostreamwrapper.ByteConsumer
import one.iolab.app.iostreamwrapper.ByteProvider
import org.slf4j.LoggerFactory

public class SCPProviderImpl(input: ByteProvider, output: ByteConsumer, interrupt: () -> Boolean) {

    private val input: ByteProvider
    private val output: ByteConsumer
    private val interrupt: () -> Boolean
    private var dirStk: MutableList<String>? = null

    init {
        this.input = input
        this.output = output
        this.interrupt = interrupt
    }

    // dir stack

    public fun setDir(dir: MutableList<String>): SCPProviderImpl {
        this.dirStk = dir
        return this
    }

    public fun getDir(): MutableList<String>? {
        return this.dirStk
    }

    private fun launchFileStream(
            input: InputStream,
            size: Int,
            buffer: ByteArray = ByteArray(1024),
    ): Int {

        var cnt: Int = 0

        try {
            while (!this.interrupt() && cnt < size) {
                val len: Int = input.read(buffer, 0, buffer.size)
                if (len == -1 && len == 0) break
                if (cnt + len > size) {
                    this.output.send(buffer, 0, size - cnt)
                } else {
                    this.output.send(buffer, 0, len)
                }
                cnt += len
            }

            this.output.send(ByteArray(1) { 0 })
            this.output.flush()
        } catch (e: IOException) {
            this.output.send(ByteArray(1) { 2 })
            this.output.send("Error Occured while send file...\n".toByteArray())
            this.output.flush()
            return cnt
        }

        this.requireAwait = true
        return cnt
    }

    private fun launchFileBytes(input: ByteArray): Int {

        try {
            this.output.send(input)
            this.output.send(ByteArray(1) { 0 })
            this.output.flush()
        } catch (e: IOException) {
            this.output.send(ByteArray(1) { 2 })
            this.output.send("Error Occured while send file...\n".toByteArray())
            this.output.flush()
            return 0
        }

        this.requireAwait = true
        return input.size
    }

    private fun launchFile(
            name: String,
            size: Int,
            mode: UInt,
    ): Boolean {
        val octModeStr = mode.toString(8)

        val line: ByteArray =
                ("C${ "0".repeat(4 - octModeStr.length) }${ octModeStr } ${ size.toString(10) } ${ name }\n")
                        .toByteArray(StandardCharsets.UTF_8)

        try {
            this.output.accept(line)
        } catch (e: IOException) {
            return false
        }

        LoggerFactory.getLogger("debug").info("file: {}", line.toString(StandardCharsets.UTF_8))

        this.requireAwait = true
        return true
    }

    private fun launchDir(
            name: String,
            mode: UInt,
    ): Boolean {
        val octModeStr = mode.toString(8)

        val line: ByteArray =
                ("D" + "0".repeat(4 - octModeStr.length) + octModeStr + " 0 " + name + "\n")
                        .toByteArray(StandardCharsets.UTF_8)

        try {
            this.output.accept(line)
        } catch (e: IOException) {
            return false
        }

        LoggerFactory.getLogger("debug").info("dir: {}", line.toString(StandardCharsets.UTF_8))

        this.requireAwait = true
        return true
    }

    public fun start(): SCPProviderImpl {
        this.requireAwait = true
        return this
    }

    public fun file(
            name: String,
            size: Int,
            stream: InputStream,
            mode: UInt = 0x1FFu, // 0777
    ): SCPProviderImpl {
        awaitIfAutoAwait()
        this.launchFile(name, size, mode)
        awaitIfAutoAwait()
        this.launchFileStream(stream, size)
        return this
    }

    public fun file(
            name: String,
            string: String,
            charset: Charset = StandardCharsets.UTF_8,
            mode: UInt = 0x1FFu, // 0777
    ): SCPProviderImpl {
        val ba = string.toByteArray(charset)
        LoggerFactory.getLogger("debug").info("file: {}[{}]: {}", name, ba.size, string)
        awaitIfAutoAwait()
        this.launchFile(name, ba.size, mode)
        awaitIfAutoAwait()
        this.launchFileBytes(ba)
        return this
    }

    public fun dir(name: String, mode: UInt = 0x1FFu): SCPProviderImpl {
        LoggerFactory.getLogger("debug").info("dir: {}", name)
        awaitIfAutoAwait()
        this.launchDir(name, mode)
        this.dirStk?.addLast(name)
        return this
    }

    public fun rid(): SCPProviderImpl {
        LoggerFactory.getLogger("debug").info("exit dir")

        awaitIfAutoAwait()
        this.output.accept("E\n".toByteArray())
        this.requireAwait = true
        if (this.dirStk != null) {
            this.dirStk = this.dirStk?.subList(0, (this.dirStk as MutableList<String>).size - 1)
        }
        return this
    }

    public fun time(mtime: ZonedDateTime, atime: ZonedDateTime): SCPProviderImpl {
        awaitIfAutoAwait()
        this.output.accept(
                String.format(
                                "T%d 0 %d 0\n",
                                mtime.toEpochSecond(),
                                atime.toEpochSecond(),
                        )
                        .toByteArray()
        )
        this.requireAwait = true
        return this
    }

    public fun exit(code: Response = Response.OK, msg: String = ""): SCPProviderImpl? {
        LoggerFactory.getLogger("debug").info("exit dir")

        awaitIfAutoAwait()
        this.output.accept(ByteArray(1) { code.toByte() })
        if (code != Response.OK) {
            this.output.accept(msg.toByteArray())
            this.output.accept("\n".toByteArray())
        }
        this.requireAwait = false
        return null
    }

    enum class Response(value: Byte) {
        OK(0),
        Warning(1),
        Error(2);

        val value: Byte

        init {
            this.value = value
        }

        public override fun toString(): String {
            when (this.value) {
                0.toByte() -> {
                    return "SCP_PROVIDER_RESPONSE_OK"
                }
                1.toByte() -> {
                    return "SCP_PROVIDER_RESPONSE_WARN"
                }
                2.toByte() -> {
                    return "SCP_PROVIDER_RESPONSE_ERROR"
                }
            }

            throw IllegalStateException()
        }

        public fun toByte(): Byte {
            return this.value
        }
    }

    public var autoAwait = false
    private var requireAwait = true
    public var response: Response? = null
    public var reason: String? = ""

    public fun autoAwait(await: Boolean): SCPProviderImpl {
        this.autoAwait = await
        return this
    }

    private fun awaitIfAutoAwait() {
        if (this.autoAwait && this.requireAwait && !this.await()) {
            throw RuntimeException("Unhandled Exception Scp Response")
        }
    }

    public fun await(): Boolean {
        LoggerFactory.getLogger("debug").info("await")
        this.response = null

        val responseArr = this.input.getByte()
        if (responseArr == null || responseArr.size == 0) {
            throw IOException("Unexpected Response Pack Received")
        }
        val response = responseArr.get(0).toInt()
        if (response < 0 || response > 3) {
            throw IllegalStateException("Unexpected Response Pack Received")
        }

        when (response) {
            0 -> {
                this.response = Response.OK
            }
            1 -> {
                this.response = Response.Warning
            }
            2 -> {
                this.response = Response.Error
            }
        }

        if (response > 0) {
            val ba: MutableList<Byte> = MutableList(0) { 0 }
            var b: Byte

            do {
                val ra = this.input.getByte()

                if (ra == null || ra.size == 0) {
                    throw IOException("Unexpected Response Pack Received")
                }
                b = ra.get(0)
                ba.add(b)
            } while (!this.interrupt() && b != '\n'.code.toByte())

            this.reason = (ByteArray(ba.size) { ba[it] }).toString(StandardCharsets.UTF_8)
            this.requireAwait = false
            return false
        }

        this.reason = null
        this.requireAwait = false
        return true
    }
}
