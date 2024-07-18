package one.iolab.app.drivers

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

public open class BaseShellDriver() : BaseDriver<Any?>() {

    companion object {
        @JvmField public val driverName: String = "BaseShell"
    }

    protected open var testStr: String = "BaseShellDriver\r\n"

    public open override fun run() {
        var charset: Charset = StandardCharsets.UTF_8

        this.cout.accept(this.testStr.toByteArray(charset))

        while (!this.thread.isInterrupted()) {
            var byte: Byte? = this.cin.getByte()[0]
            if (byte == null || byte == 3.toByte()) {
                break
            }
            this.cout.accept(String.format("<%d>", byte.toInt()).toByteArray())
        }

        this.callback.onExit(0, "recv ^C")
        return
    }
}
