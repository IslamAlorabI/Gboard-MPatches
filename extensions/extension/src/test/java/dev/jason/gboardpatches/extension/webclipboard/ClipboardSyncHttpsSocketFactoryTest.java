package dev.jason.gboardpatches.extension.webclipboard;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import org.junit.Assert;
import org.junit.Test;

public final class ClipboardSyncHttpsSocketFactoryTest {
    @Test
    public void createsSelfSignedKeyAndReusesItFromDisk() throws Exception {
        File directory = Files.createTempDirectory("web-clipboard-tls").toFile();
        try {
            KeyStore first = ClipboardSyncHttpsSocketFactory.loadOrCreateKeyStore(directory);
            X509Certificate certificate = (X509Certificate) first.getCertificate(
                    ClipboardSyncHttpsSocketFactory.KEY_ALIAS);
            certificate.checkValidity();
            certificate.verify(certificate.getPublicKey());
            Assert.assertTrue(certificate.getSubjectX500Principal().getName()
                    .contains(ClipboardSyncSelfSignedCertificate.COMMON_NAME));
            Assert.assertTrue(new File(directory,
                    ClipboardSyncHttpsSocketFactory.KEYSTORE_FILE_NAME).isFile());

            KeyStore second = ClipboardSyncHttpsSocketFactory.loadOrCreateKeyStore(directory);

            Assert.assertEquals(certificate, second.getCertificate(
                    ClipboardSyncHttpsSocketFactory.KEY_ALIAS));
        } finally {
            for (File file : directory.listFiles()) {
                file.delete();
            }
            directory.delete();
        }
    }

    @Test
    public void separateInstallsGetDifferentKeys() throws Exception {
        KeyStore first = ClipboardSyncHttpsSocketFactory.loadOrCreateKeyStore(null);
        KeyStore second = ClipboardSyncHttpsSocketFactory.loadOrCreateKeyStore(null);

        Assert.assertNotEquals(
                first.getCertificate(ClipboardSyncHttpsSocketFactory.KEY_ALIAS).getPublicKey(),
                second.getCertificate(ClipboardSyncHttpsSocketFactory.KEY_ALIAS).getPublicKey());
    }
}
