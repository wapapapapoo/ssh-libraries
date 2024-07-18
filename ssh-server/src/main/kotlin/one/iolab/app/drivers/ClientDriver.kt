package one.iolab.app.drivers

public open class ClientDriver() : BaseShellDriver() {

    companion object {
        @JvmField public val driverName: String = "Client"
    }

    override var testStr: String = "ClientDriver\r\n"
}
