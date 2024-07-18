package one.iolab.app.drivers

public open class TUIShellDriver() : BaseShellDriver() {

    companion object {
        @JvmField public val driverName: String = "TUIShell"
    }

    override var testStr: String = "TUIShellDriver\r\n"
}
