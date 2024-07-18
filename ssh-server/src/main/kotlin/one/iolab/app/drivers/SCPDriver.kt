package one.iolab.app.drivers

import one.iolab.utils.CommandUtils.SCPArgs
import one.iolab.utils.SCPProviderImpl

public open class SCPDriver() : BaseDriver<SCPArgs?>() {

    companion object {
        @JvmField public val driverName: String = "SCP"
    }

    public override fun run() {

        var scp: SCPProviderImpl =
                SCPProviderImpl(this.cin, this.cout, { this.thread.isInterrupted() })

        scp.setDir(MutableList(0) { "" })
                .autoAwait(true)
                .start()
                .dir("dir1")
                .file("awa.txt", "hello\n")
                .dir("dir2")
                .file("pwp.txt", "pwp")
                .file("log.txt", scp.getDir().toString())
                .rid()
                .rid()
                .exit()

        // var charset: Charset = StandardCharsets.UTF_8

        // this.cin.getByte()
        // this.cout.accept("D0777 0 dir1\n".toByteArray(charset))

        // this.cin.getByte()
        // this.cout.accept("C0777 6 tessocr\n".toByteArray(charset))

        // this.cin.getByte()
        // this.cout.accept("hellow".toByteArray())
        // this.cout.accept(ByteArray(1) { 0 })

        // this.cin.getByte()
        // this.cout.accept("D0777 0 dir2\n".toByteArray(charset))

        // this.cin.getByte()
        // this.cout.accept("C0777 7 awa\n".toByteArray(charset))

        // this.cin.getByte()
        // this.cout.accept("hellow\n".toByteArray(charset))
        // this.cout.accept(ByteArray(1) { 0 })

        // this.cin.getByte()
        // this.cout.accept("E\n".toByteArray(charset))

        // this.cin.getByte()
        // this.cout.accept("E\n".toByteArray(charset))

        // this.cin.getByte()
        // this.cout.accept(ByteArray(1) { 0 })

        // this.callback.onExit(0)
    }
}
