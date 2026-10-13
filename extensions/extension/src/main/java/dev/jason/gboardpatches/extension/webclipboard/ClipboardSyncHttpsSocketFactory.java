package dev.jason.gboardpatches.extension.webclipboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

/**
 * HTTPS server sockets for the Web Clipboard portal. Each install creates its own key on first
 * use and keeps it in the app's private, non-backed-up storage.
 */
final class ClipboardSyncHttpsSocketFactory {
    static final String KEYSTORE_FILE_NAME = "web_clipboard_tls.p12";
    static final String KEY_ALIAS = "web-clipboard";
    // Only satisfies the PKCS12 format; the file itself is private to the app.
    private static final char[] KEYSTORE_PASSWORD = "gboard-web-clipboard".toCharArray();
    private static volatile File keyStoreDirectory;
    private static volatile SSLContext cachedSslContext;

    private ClipboardSyncHttpsSocketFactory() {
    }

    /** Where to keep the per-install key. Without a directory, the key lasts for this process. */
    static void useKeyStoreDirectory(File directory) {
        keyStoreDirectory = directory;
    }

    static int defaultHttpsPortFor(int httpPort) {
        if (httpPort <= 0) {
            return 0;
        }
        return httpPort < 65535 ? httpPort + 1 : httpPort - 1;
    }

    static ServerSocket createServerSocket(int port) throws Exception {
        SSLServerSocket socket = (SSLServerSocket) sslContext()
                .getServerSocketFactory()
                .createServerSocket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(port));
        return socket;
    }

    private static SSLContext sslContext() throws Exception {
        SSLContext current = cachedSslContext;
        if (current != null) {
            return current;
        }

        synchronized (ClipboardSyncHttpsSocketFactory.class) {
            current = cachedSslContext;
            if (current != null) {
                return current;
            }

            KeyStore keyStore = loadOrCreateKeyStore(keyStoreDirectory);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            cachedSslContext = sslContext;
            return sslContext;
        }
    }

    static KeyStore loadOrCreateKeyStore(File directory) throws Exception {
        File file = directory == null ? null : new File(directory, KEYSTORE_FILE_NAME);
        if (file != null && file.isFile()) {
            try (InputStream input = new FileInputStream(file)) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(input, KEYSTORE_PASSWORD);
                if (keyStore.isKeyEntry(KEY_ALIAS)) {
                    return keyStore;
                }
            } catch (Exception ignored) {
                // An unreadable key file is replaced with a new key below.
            }
        }

        KeyStore keyStore = ClipboardSyncSelfSignedCertificate.createKeyStore(
                KEY_ALIAS, KEYSTORE_PASSWORD);
        if (file != null) {
            File temporary = new File(directory, KEYSTORE_FILE_NAME + ".tmp");
            try (OutputStream output = new FileOutputStream(temporary)) {
                keyStore.store(output, KEYSTORE_PASSWORD);
            } catch (Exception ignored) {
                // Saving is best effort; the key still works until the process ends.
            }
            if (!temporary.renameTo(file)) {
                temporary.delete();
            }
        }
        return keyStore;
    }
}
