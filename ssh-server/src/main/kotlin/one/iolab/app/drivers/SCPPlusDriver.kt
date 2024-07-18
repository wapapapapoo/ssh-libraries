package one.iolab.app.drivers

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

public open class SCPPlusDriver() : BaseDriver<Array<String>>() {

    companion object {
        @JvmField public val driverName: String = "SCP+"
    }

    public override fun run() {
        var charset: Charset = StandardCharsets.UTF_8

        this.cout.accept("C0777 6 tessocr\n".toByteArray(charset))
        this.cin.getByte()
        this.cout.accept("hellow".toByteArray(charset))
        this.cin.getByte()
        this.cout.accept(ByteArray(1) { 0 })
        this.cin.getByte()

        this.callback.onExit(0)
        return
    }
}
