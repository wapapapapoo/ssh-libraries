package one.iolab;

import java.security.KeyPair;

import javax.crypto.Cipher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import one.iolab.app.sshdconfig.MyKeyPairProvider;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    // public void testApp()
    // {
    // assertTrue(true);
    // }

    public void testKeyPairProvider() throws Exception {
        String testStr = "awa";
        byte[] testByte = testStr.getBytes();
        byte[] encodedByte = null;
        String resulStr = "pwp";

        KeyPair keyPair = new MyKeyPairProvider().loadKey(null, "ssh-rsa");

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        encodedByte = cipher.doFinal(testByte);

        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        testByte = cipher.doFinal(encodedByte);

        resulStr = new String(testByte);

        assertTrue(testStr.equals(resulStr));
    }
}
