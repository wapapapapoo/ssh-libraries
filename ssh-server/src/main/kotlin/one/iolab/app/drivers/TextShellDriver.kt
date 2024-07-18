package one.iolab.app.drivers

public open class TextShellDriver() : BaseShellDriver() {

    companion object {
        @JvmField public val driverName: String = "TextShell"
    }

    override var testStr: String = "TextShellDriver\r\n"
}
