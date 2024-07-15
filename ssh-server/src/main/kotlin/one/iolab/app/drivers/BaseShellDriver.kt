package one.iolab.app.drivers

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

import one.iolab.app.drivers.BaseDriver

public open class BaseShellDriver() : BaseDriver() {

    public override fun run() {
        var charset: Charset = StandardCharsets.UTF_8

        this.cout.accept("pong\r\n".toByteArray(charset))

        this.exitCallback.onExit(0);
        return;
    }

}
