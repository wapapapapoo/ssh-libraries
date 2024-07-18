package one.iolab.app.sshdconfig;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;

import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class MyKeyPairProvider implements KeyPairProvider {

    // private Logger logger = LoggerFactory.getLogger(MyKeyPairProvider.class);

    @Override
    public KeyPair loadKey(SessionContext session, String type) {

        InputStream is;
        InputStreamReader isr;
        BufferedReader br;

        String privateKeyString = "";
        String publicKeyString = "";

        try {
            String line;

            is = this.getClass().getClassLoader()
                    .getResourceAsStream("key-pairs/server_rsa_key.pem");
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                if (!line.startsWith("-----")) {
                    privateKeyString += line;
                }
            }

            br.close();
            isr.close();
            is.close();

            is = this.getClass().getClassLoader()
                    .getResourceAsStream("key-pairs/server_rsa_pub_key.pem");
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                if (!line.startsWith("-----")) {
                    publicKeyString += line;
                }
            }

            br.close();
            isr.close();
            is.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);

        try {

            // 加载公钥
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = java.security.KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);

            // 加载私钥
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = java.security.KeyFactory.getInstance("RSA").generatePrivate(privateKeySpec);

            // 创建KeyPair对象
            return new KeyPair(publicKey, privateKey);

        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Reached impossible corner.");
    }

    @Override
    public Iterable<String> getKeyTypes(SessionContext session) {
        return Collections.singleton(SSH_RSA);
    }

    @Override
    public Iterable<KeyPair> loadKeys(SessionContext session) {

        return Collections.singleton(this.loadKey(null, null));
    }

    @Override
    public String toString() {
        return "TEST_KEPAIR_PROVIDER";
    }

}
